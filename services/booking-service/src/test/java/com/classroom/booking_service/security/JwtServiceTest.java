package com.classroom.booking_service.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generateAndValidateToken() {
        // secret must be at least 32 bytes for HS256
        String secret = "01234567890123456789012345678901";
        JwtService svc = new JwtService(secret, 1000L);

        String token = svc.generateToken("alice");
        assertNotNull(token);

        Claims claims = svc.validate(token);
        assertEquals("alice", claims.getSubject());
    }
}
