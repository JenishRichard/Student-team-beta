package com.classroom.api_gateway.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class JwtAuthFilter implements Filter {

  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;

    String path = req.getRequestURI();

    // allow auth endpoints
    if (path.startsWith("/auth")) {
      chain.doFilter(request, response);
      return;
    }

    // protect only these
    boolean protectedPath = path.startsWith("/rooms") || path.startsWith("/bookings");
    if (!protectedPath) {
      chain.doFilter(request, response);
      return;
    }

    String auth = req.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      send401(res, "Missing Bearer token");
      return;
    }

    String token = auth.substring("Bearer ".length());

    try {
      jwtService.validate(token);
      chain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException e) {
      send401(res, "Invalid token");
    }
  }

  private void send401(HttpServletResponse res, String message) throws IOException {
    if (res.isCommitted()) return;
    res.resetBuffer();
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    res.setContentType("application/json");
    res.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    res.flushBuffer();
  }
}
