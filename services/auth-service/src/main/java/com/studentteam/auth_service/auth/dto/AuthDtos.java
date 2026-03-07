package com.studentteam.auth_service.auth.dto;

import com.studentteam.auth_service.auth.entity.UserStatus;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AuthDtos {

  public record RegisterRequest(
      String userId,
      @NotBlank String email,
      @NotBlank String password,
      List<String> roles
  ) {}

  public record LoginRequest(
      @NotBlank String email,
      @NotBlank String password
  ) {}

  public record AuthResponse(
      String accessToken,
      String tokenType
  ) {}

  public record MeResponse(
      String email,
      List<String> roles
  ) {}

  public record UserResponse(
      String userId,
      String email,
      String role,
      UserStatus status
  ) {}

  public record UpdateUserRequest(
      String email,
      String password,
      String role,
      UserStatus status
  ) {}
}
