package com.studentteam.auth_service.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentteam.auth_service.auth.dto.AuthDtos;
import com.studentteam.auth_service.auth.exception.InvalidCredentialsException;
import com.studentteam.auth_service.auth.jwt.JwtService;
import com.studentteam.auth_service.auth.service.AuthService;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc(addFilters = false)
@Import(AuthExceptionHandler.class)
@WebMvcTest(controllers = AuthController.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "jwt.secret=test-secret-key-at-least-32-characters",
        "jwt.expiration-ms=3600000"
})
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;

  @MockBean
  private JwtService jwtService;

  @Autowired
  private AuthExceptionHandler authExceptionHandler;

  @Test
  void login_shouldReturn200AndBearerTokenForValidCredentials() throws Exception {
    when(authService.login(any(AuthDtos.LoginRequest.class)))
        .thenReturn(new AuthDtos.AuthResponse("jwt-token", "Bearer"));

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest("admin@tus.ie", "Admin@123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("jwt-token"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"));
  }

  @Test
  void login_shouldReturn401ForInvalidCredentials() throws Exception {
    when(authService.login(any(AuthDtos.LoginRequest.class)))
        .thenThrow(new InvalidCredentialsException("Invalid credentials"));

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new AuthDtos.LoginRequest("admin@tus.ie", "wrong-password"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(jsonPath("$.message").value("Invalid credentials"));
  }

  @Test
  void login_shouldReturn401WhenInvalidCredentialsAreWrappedInServletException() throws Exception {
    ResponseEntity<Map<String, Object>> response =
        authExceptionHandler.handleAny(new ServletException(new InvalidCredentialsException("Invalid credentials")));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Unauthorized", response.getBody().get("error"));
    assertEquals("Invalid credentials", response.getBody().get("message"));
  }
}
