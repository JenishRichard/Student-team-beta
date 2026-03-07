package com.studentteam.auth_service.auth.repository;

import com.studentteam.auth_service.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmailIgnoreCase(String email);
  Optional<UserAccount> findByUserIdIgnoreCase(String userId);
  boolean existsByEmailIgnoreCase(String email);
  boolean existsByUserIdIgnoreCase(String userId);
}
