package com.studentteam.auth_service.auth.controller;

import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<AuthDtos.UserResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.createUser(req));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    return ResponseEntity.ok(authService.login(req));
  }

  @GetMapping("/users")
  public ResponseEntity<List<AuthDtos.UserResponse>> getUsers() {
    return ResponseEntity.ok(authService.listUsers());
  }

  @PutMapping("/users/{userId}")
  public ResponseEntity<AuthDtos.UserResponse> updateUser(
      @PathVariable String userId,
      @RequestBody AuthDtos.UpdateUserRequest req) {
    return ResponseEntity.ok(authService.updateUser(userId, req));
  }

  @DeleteMapping("/users/{userId}")
  public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
    authService.deleteUser(userId);
    return ResponseEntity.noContent().build();
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
