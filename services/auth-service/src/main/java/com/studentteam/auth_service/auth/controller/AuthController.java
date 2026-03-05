package com.studentteam.auth_service.auth.controller;

import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    authService.register(req);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    return ResponseEntity.ok(authService.login(req));
  }

  @GetMapping("/me")
  public ResponseEntity<AuthDtos.MeResponse> me(@RequestHeader("Authorization") String authHeader) {
    String token = extractBearer(authHeader);
    Claims claims = jwtService.parseAndValidate(token).getBody();

    String email = claims.getSubject();
    @SuppressWarnings("unchecked")
    List<String> roles = (List<String>) claims.get("roles", List.class);

    return ResponseEntity.ok(new AuthDtos.MeResponse(email, roles));
  }

  private String extractBearer(String header) {
    if (header == null || !header.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Missing/invalid Authorization header");
    }
    return header.substring("Bearer ".length());
  }
}
