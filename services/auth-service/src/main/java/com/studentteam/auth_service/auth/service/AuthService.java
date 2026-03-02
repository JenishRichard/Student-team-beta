package com.studentteam.auth_service.auth.service;

import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.store.InMemoryUserStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

  private final InMemoryUserStore store;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;

  public AuthService(InMemoryUserStore store, PasswordEncoder encoder, JwtService jwtService) {
    this.store = store;
    this.encoder = encoder;
    this.jwtService = jwtService;
  }

  public void register(AuthDtos.RegisterRequest req) {
    if (store.exists(req.email())) {
      throw new IllegalArgumentException("User already exists");
    }

    List<String> roles = (req.roles() == null || req.roles().isEmpty())
        ? List.of("USER")
        : req.roles();

    String hash = encoder.encode(req.password());
    store.save(new InMemoryUserStore.UserRecord(req.email(), hash, roles));
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
    var user = store.findByEmail(req.email())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!encoder.matches(req.password(), user.passwordHash)) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    String token = jwtService.generateAccessToken(user.email, user.roles);
    return new AuthDtos.AuthResponse(token, "Bearer");
  }
}
