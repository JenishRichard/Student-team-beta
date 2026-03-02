package com.studentteam.auth_service.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class AuthDtos {

  public record RegisterRequest(
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
}
