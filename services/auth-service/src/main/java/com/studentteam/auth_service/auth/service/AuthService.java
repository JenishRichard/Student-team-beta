package com.studentteam.auth_service.auth.service;

import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.entity.UserAccount;
import com.studentteam.auth_service.auth.entity.UserStatus;
import com.studentteam.auth_service.auth.exception.InvalidCredentialsException;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.repository.UserAccountRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class AuthService {

  private static final List<String> ALLOWED_ROLES = List.of("SUPER_ADMIN", "ADMIN", "TEACHER", "STUDENT");

  private final UserAccountRepository repository;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder encoder;
  private final JwtService jwtService;

  public AuthService(
      UserAccountRepository repository,
      AuthenticationManager authenticationManager,
      PasswordEncoder encoder,
      JwtService jwtService
  ) {
    this.repository = repository;
    this.authenticationManager = authenticationManager;
    this.encoder = encoder;
    this.jwtService = jwtService;
  }

  @Transactional
  public void register(AuthDtos.RegisterRequest req) {
    createUser(req);
  }

  @Transactional(readOnly = true)
  public List<AuthDtos.UserResponse> listUsers() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(UserAccount::getUserId, String.CASE_INSENSITIVE_ORDER))
        .map(this::toUserResponse)
        .toList();
  }

  @Transactional
  public AuthDtos.UserResponse createUser(AuthDtos.RegisterRequest req) {
    String normalizedEmail = normalizeEmail(req.email());
    String normalizedRole = normalizeRole(primaryRole(req.roles()));
    String normalizedUserId = normalizeUserId(req.userId());

    if (repository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new IllegalArgumentException("User already exists");
    }
    if (repository.existsByUserIdIgnoreCase(normalizedUserId)) {
      throw new IllegalArgumentException("User ID already exists");
    }

    UserAccount user = new UserAccount();
    user.setUserId(normalizedUserId);
    user.setEmail(normalizedEmail);
    user.setPasswordHash(encoder.encode(req.password()));
    user.setRole(normalizedRole);
    user.setStatus(UserStatus.ACTIVE);

    UserAccount saved = repository.save(user);
    return toUserResponse(saved);
  }

  @Transactional
  public AuthDtos.UserResponse updateUser(String userId, AuthDtos.UpdateUserRequest req) {
    UserAccount user = repository.findByUserIdIgnoreCase(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found"));

    if (req.email() != null && !req.email().isBlank()) {
      String normalizedEmail = normalizeEmail(req.email());
      repository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
        if (!existing.getUserId().equalsIgnoreCase(user.getUserId())) {
          throw new IllegalArgumentException("Email already exists");
        }
      });
      user.setEmail(normalizedEmail);
    }

    if (req.password() != null && !req.password().isBlank()) {
      user.setPasswordHash(encoder.encode(req.password().trim()));
    }

    if (req.role() != null && !req.role().isBlank()) {
      user.setRole(normalizeRole(req.role()));
    }

    if (req.status() != null) {
      user.setStatus(req.status());
    }

    UserAccount saved = repository.save(user);
    return toUserResponse(saved);
  }

  @Transactional
  public void deleteUser(String userId) {
    UserAccount user = repository.findByUserIdIgnoreCase(userId)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
    repository.delete(user);
  }

  public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
    String normalizedEmail = normalizeEmail(req.email());
    authenticate(normalizedEmail, req.password());
    UserAccount user = repository.findByEmailIgnoreCase(normalizedEmail)
        .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

    String token = jwtService.generateAccessToken(user.getEmail(), List.of(user.getRole()));
    return new AuthDtos.AuthResponse(token, "Bearer");
  }

  private AuthDtos.UserResponse toUserResponse(UserAccount user) {
    return new AuthDtos.UserResponse(
        user.getUserId(),
        user.getEmail(),
        user.getRole(),
        user.getStatus()
    );
  }

  private String normalizeEmail(String email) {
    if (email == null) throw new IllegalArgumentException("Email is required");
    String normalized = email.trim().toLowerCase(Locale.ROOT);
    if (normalized.isEmpty()) throw new IllegalArgumentException("Email is required");
    return normalized;
  }

  private void authenticate(String email, String password) {
    try {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    } catch (DisabledException ex) {
      throw new InvalidCredentialsException("User account is not active");
    } catch (AuthenticationException ex) {
      throw new InvalidCredentialsException("Invalid credentials");
    }
  }

  private String normalizeRole(String role) {
    String normalized = role.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    if (!ALLOWED_ROLES.contains(normalized)) {
      throw new IllegalArgumentException("Invalid role");
    }
    return normalized;
  }

  private String normalizeUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      return "USR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
    return userId.trim().toUpperCase(Locale.ROOT);
  }

  private String primaryRole(List<String> roles) {
    if (roles == null || roles.isEmpty() || roles.get(0) == null || roles.get(0).isBlank()) {
      return "STUDENT";
    }
    return roles.get(0);
  }
}
