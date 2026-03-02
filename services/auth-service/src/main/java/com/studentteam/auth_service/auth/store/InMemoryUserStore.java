package com.studentteam.auth_service.auth.store;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryUserStore {

  public static class UserRecord {
    public final String email;
    public final String passwordHash;
    public final List<String> roles;

    public UserRecord(String email, String passwordHash, List<String> roles) {
      this.email = email;
      this.passwordHash = passwordHash;
      this.roles = roles;
    }
  }

  private final Map<String, UserRecord> users = new ConcurrentHashMap<>();

  public Optional<UserRecord> findByEmail(String email) {
    return Optional.ofNullable(users.get(email.toLowerCase()));
  }

  public boolean exists(String email) {
    return users.containsKey(email.toLowerCase());
  }

  public void save(UserRecord user) {
    users.put(user.email.toLowerCase(), user);
  }
}
