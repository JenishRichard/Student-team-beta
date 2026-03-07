package com.studentteam.auth_service.auth.controller;

import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
    if (ex instanceof JwtException) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Unauthorized", "message", ex.getMessage()));
    }
    if (ex instanceof NoSuchElementException) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Not Found", "message", ex.getMessage()));
    }
    if (ex instanceof IllegalArgumentException) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Bad Request", "message", ex.getMessage()));
    }
    if (ex instanceof DataIntegrityViolationException) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("error", "Conflict", "message", "Duplicate value"));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of("error", "Internal Server Error", "message", ex.getClass().getName()));
  }
}
