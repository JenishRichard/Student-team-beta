package com.studentteam.auth_service.auth.service;

import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.entity.UserAccount;
import com.studentteam.auth_service.auth.entity.UserStatus;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserAccountRepository repository;

  @Mock
  private PasswordEncoder encoder;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthService authService;

  @Test
  void createUser_shouldNormalizeAndPersistValues() {
    AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
        "admin001",
        " Admin@TUS.ie ",
        "Admin@123",
        List.of("super admin")
    );

    when(repository.existsByEmailIgnoreCase("admin@tus.ie")).thenReturn(false);
    when(repository.existsByUserIdIgnoreCase("ADMIN001")).thenReturn(false);
    when(encoder.encode("Admin@123")).thenReturn("ENCODED");
    when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AuthDtos.UserResponse response = authService.createUser(request);

    ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
    verify(repository).save(captor.capture());
    UserAccount saved = captor.getValue();

    assertEquals("ADMIN001", saved.getUserId());
    assertEquals("admin@tus.ie", saved.getEmail());
    assertEquals("SUPER_ADMIN", saved.getRole());
    assertEquals("ENCODED", saved.getPasswordHash());
    assertEquals(UserStatus.ACTIVE, saved.getStatus());
    assertEquals("ADMIN001", response.userId());
    assertEquals("admin@tus.ie", response.email());
    assertEquals("SUPER_ADMIN", response.role());
    assertEquals(UserStatus.ACTIVE, response.status());
  }

  @Test
  void createUser_shouldDefaultRoleToStudentWhenRolesMissing() {
    AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
        "stu01",
        "student@tus.ie",
        "Password@123",
        null
    );

    when(repository.existsByEmailIgnoreCase("student@tus.ie")).thenReturn(false);
    when(repository.existsByUserIdIgnoreCase("STU01")).thenReturn(false);
    when(encoder.encode("Password@123")).thenReturn("ENCODED");
    when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AuthDtos.UserResponse response = authService.createUser(request);

    assertEquals("STUDENT", response.role());
  }

  @Test
  void createUser_shouldThrowWhenEmailAlreadyExists() {
    AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
        "admin001",
        "admin@tus.ie",
        "Admin@123",
        List.of("ADMIN")
    );

    when(repository.existsByEmailIgnoreCase("admin@tus.ie")).thenReturn(true);

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.createUser(request));
    assertEquals("User already exists", ex.getMessage());
  }

  @Test
  void createUser_shouldThrowWhenRoleInvalid() {
    AuthDtos.RegisterRequest request = new AuthDtos.RegisterRequest(
        "u1",
        "u1@tus.ie",
        "Pass@123",
        List.of("MANAGER")
    );

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> authService.createUser(request));
    assertEquals("Invalid role", ex.getMessage());
  }

  @Test
  void login_shouldReturnBearerTokenForValidCredentials() {
    UserAccount user = user("ADMIN001", "admin@tus.ie", "HASHED", "ADMIN", UserStatus.ACTIVE);
    when(repository.findByEmailIgnoreCase("admin@tus.ie")).thenReturn(Optional.of(user));
    when(encoder.matches("Admin@123", "HASHED")).thenReturn(true);
    when(jwtService.generateAccessToken("admin@tus.ie", List.of("ADMIN"))).thenReturn("jwt-token");

    AuthDtos.AuthResponse response = authService.login(new AuthDtos.LoginRequest("admin@tus.ie", "Admin@123"));

    assertEquals("jwt-token", response.accessToken());
    assertEquals("Bearer", response.tokenType());
    verify(jwtService).generateAccessToken("admin@tus.ie", List.of("ADMIN"));
  }

  @Test
  void login_shouldThrowWhenPasswordInvalid() {
    UserAccount user = user("ADMIN001", "admin@tus.ie", "HASHED", "ADMIN", UserStatus.ACTIVE);
    when(repository.findByEmailIgnoreCase("admin@tus.ie")).thenReturn(Optional.of(user));
    when(encoder.matches("wrong", "HASHED")).thenReturn(false);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> authService.login(new AuthDtos.LoginRequest("admin@tus.ie", "wrong"))
    );

    assertEquals("Invalid credentials", ex.getMessage());
  }

  @Test
  void updateUser_shouldUpdateEmailPasswordRoleAndStatus() {
    UserAccount user = user("TEACHER001", "teacher@tus.ie", "OLD_HASH", "TEACHER", UserStatus.ACTIVE);
    when(repository.findByUserIdIgnoreCase("TEACHER001")).thenReturn(Optional.of(user));
    when(repository.findByEmailIgnoreCase("new.teacher@tus.ie")).thenReturn(Optional.empty());
    when(encoder.encode("NewPass@123")).thenReturn("NEW_HASH");
    when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AuthDtos.UpdateUserRequest request = new AuthDtos.UpdateUserRequest(
        "new.teacher@tus.ie",
        "NewPass@123",
        "student",
        UserStatus.INVITED
    );

    AuthDtos.UserResponse response = authService.updateUser("TEACHER001", request);

    assertEquals("new.teacher@tus.ie", response.email());
    assertEquals("STUDENT", response.role());
    assertEquals(UserStatus.INVITED, response.status());
    assertEquals("NEW_HASH", user.getPasswordHash());
  }

  @Test
  void deleteUser_shouldThrowWhenNotFound() {
    when(repository.findByUserIdIgnoreCase("MISSING")).thenReturn(Optional.empty());

    assertThrows(NoSuchElementException.class, () -> authService.deleteUser("MISSING"));
  }

  @Test
  void listUsers_shouldSortByUserIdCaseInsensitive() {
    UserAccount b = user("B-02", "b@tus.ie", "h1", "ADMIN", UserStatus.ACTIVE);
    UserAccount a = user("a-01", "a@tus.ie", "h2", "TEACHER", UserStatus.ACTIVE);
    when(repository.findAll()).thenReturn(List.of(b, a));

    List<AuthDtos.UserResponse> users = authService.listUsers();

    assertEquals(2, users.size());
    assertEquals("a-01", users.get(0).userId());
    assertEquals("B-02", users.get(1).userId());
    assertTrue(users.stream().allMatch(u -> u.status() == UserStatus.ACTIVE));
  }

  private static UserAccount user(String userId, String email, String passwordHash, String role, UserStatus status) {
    UserAccount user = new UserAccount();
    user.setUserId(userId);
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    user.setRole(role);
    user.setStatus(status);
    return user;
  }
}
