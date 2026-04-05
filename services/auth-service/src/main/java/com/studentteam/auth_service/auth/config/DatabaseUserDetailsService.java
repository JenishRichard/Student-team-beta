package com.studentteam.auth_service.auth.config;

import com.studentteam.auth_service.auth.entity.UserAccount;
import com.studentteam.auth_service.auth.entity.UserStatus;
import com.studentteam.auth_service.auth.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

  private final UserAccountRepository userAccountRepository;

  public DatabaseUserDetailsService(UserAccountRepository userAccountRepository) {
    this.userAccountRepository = userAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizeUsername(username))
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return User.withUsername(user.getEmail())
        .password(user.getPasswordHash())
        .authorities(authoritiesFor(user.getRole()))
        .disabled(user.getStatus() != UserStatus.ACTIVE)
        .build();
  }

  private String normalizeUsername(String username) {
    if (username == null || username.isBlank()) {
      throw new UsernameNotFoundException("User not found");
    }
    return username.trim();
  }

  private List<SimpleGrantedAuthority> authoritiesFor(String role) {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role));
  }
}
