package com.studentteam.auth_service.auth.controller;

import com.studentteam.auth_service.auth.exception.InvalidCredentialsException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
    Throwable cause = unwrap(ex);

    if (cause instanceof JwtException) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Unauthorized", "message", cause.getMessage()));
    }
    if (cause instanceof InvalidCredentialsException) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Unauthorized", "message", cause.getMessage()));
    }
    if (cause instanceof AuthenticationException || cause instanceof UsernameNotFoundException) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Unauthorized", "message", "Invalid credentials"));
    }
    if (cause instanceof NoSuchElementException) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Not Found", "message", cause.getMessage()));
    }
    if (cause instanceof IllegalArgumentException) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Bad Request", "message", cause.getMessage()));
    }
    if (cause instanceof DataIntegrityViolationException) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("error", "Conflict", "message", "Duplicate value"));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal Server Error", "message", cause.getClass().getName()));
  }

  private Throwable unwrap(Throwable throwable) {
    Throwable current = throwable;
    while (current instanceof ServletException && current.getCause() != null) {
      current = current.getCause();
    }
    return current;
  }
}
