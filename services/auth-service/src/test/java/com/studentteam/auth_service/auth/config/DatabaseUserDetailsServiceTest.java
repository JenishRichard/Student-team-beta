package com.studentteam.auth_service.auth.config;

import com.studentteam.auth_service.auth.entity.UserAccount;
import com.studentteam.auth_service.auth.entity.UserStatus;
import com.studentteam.auth_service.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

  @Mock
  private UserAccountRepository userAccountRepository;

  @InjectMocks
  private DatabaseUserDetailsService databaseUserDetailsService;

  @Test
  void loadUserByUsername_shouldBuildEnabledPrincipalForActiveUser() {
    UserAccount user = user("admin@tus.ie", "HASHED", "ADMIN", UserStatus.ACTIVE);
    when(userAccountRepository.findByEmailIgnoreCase("admin@tus.ie")).thenReturn(Optional.of(user));

    UserDetails userDetails = databaseUserDetailsService.loadUserByUsername(" admin@tus.ie ");

    assertEquals("admin@tus.ie", userDetails.getUsername());
    assertEquals("HASHED", userDetails.getPassword());
    assertTrue(userDetails.isEnabled());
    assertTrue(userDetails.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
  }

  @Test
  void loadUserByUsername_shouldDisableInvitedUsers() {
    UserAccount user = user("teacher@tus.ie", "HASHED", "TEACHER", UserStatus.INVITED);
    when(userAccountRepository.findByEmailIgnoreCase("teacher@tus.ie")).thenReturn(Optional.of(user));

    UserDetails userDetails = databaseUserDetailsService.loadUserByUsername("teacher@tus.ie");

    assertFalse(userDetails.isEnabled());
  }

  @Test
  void loadUserByUsername_shouldThrowWhenUserMissing() {
    when(userAccountRepository.findByEmailIgnoreCase("missing@tus.ie")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
        () -> databaseUserDetailsService.loadUserByUsername("missing@tus.ie"));
  }

  private static UserAccount user(String email, String passwordHash, String role, UserStatus status) {
    UserAccount user = new UserAccount();
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    user.setRole(role);
    user.setStatus(status);
    return user;
  }
}
