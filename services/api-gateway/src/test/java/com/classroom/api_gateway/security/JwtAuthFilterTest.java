package com.classroom.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthFilterTest {

  private static final String SECRET = "CHANGE_THIS_TO_A_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS_123456";

  private final RecordingFilterChain chain = new RecordingFilterChain();
  private final TestJwtService jwtService = new TestJwtService();
  private final JwtAuthFilter filter = new JwtAuthFilter(jwtService);

  @Test
  void shouldAllowAuthEndpointWithoutToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    assertTrue(chain.called);
    assertEquals(0, jwtService.validateCalls);
  }

  @Test
  void shouldReturn401WhenProtectedRoomEndpointHasNoToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Missing Bearer token"));
    assertEquals(0, jwtService.validateCalls);
  }

  @Test
  void shouldReturn401WhenTokenIsInvalid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms/1");
    request.addHeader("Authorization", "Bearer bad-token");
    MockHttpServletResponse response = new MockHttpServletResponse();
    jwtService.throwOnValidate = true;

    filter.doFilter(request, response, chain);

    assertEquals(401, response.getStatus());
    assertTrue(response.getContentAsString().contains("Invalid token"));
  }

  @Test
  void shouldAllowProtectedRoomEndpointWithValidToken() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms/1");
    request.addHeader("Authorization", "Bearer " + validToken());
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    assertEquals(1, jwtService.validateCalls);
    assertTrue(chain.called);
  }

  @Test
  void shouldShortCircuitPreflightRequests() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/rooms");
    request.addHeader("Origin", "http://localhost:5173");
    MockHttpServletResponse response = new MockHttpServletResponse();

    filter.doFilter(request, response, chain);

    assertEquals(200, response.getStatus());
    assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
    assertTrue(!chain.called);
  }

  private static String validToken() {
    Key key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    Date now = new Date();
    Date expiry = new Date(now.getTime() + 60_000);

    return Jwts.builder()
        .setSubject("admin@tus.ie")
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  private static final class RecordingFilterChain implements FilterChain {
    private boolean called;

    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response) {
      this.called = true;
    }
  }

  private static final class TestJwtService extends JwtService {
    private boolean throwOnValidate;
    private int validateCalls;

    private TestJwtService() {
      super(SECRET);
    }

    @Override
    public Claims validate(String token) {
      validateCalls++;
      if (throwOnValidate) {
        throw new JwtException("bad token");
      }
      return super.validate(token);
    }
  }
}
