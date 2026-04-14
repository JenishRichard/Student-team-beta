package com.classroom.booking_service.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void passwordEncoderIsBCrypt() {
        SecurityConfig cfg = new SecurityConfig(Mockito.mock(JwtAuthenticationFilter.class));
        var encoder = cfg.passwordEncoder();
        assertNotNull(encoder);
        assertTrue(encoder.matches("pass", encoder.encode("pass")));
    }

    @Test
    void userDetailsServiceCreatesUserWithEncodedPassword() {
        SecurityConfig cfg = new SecurityConfig(Mockito.mock(JwtAuthenticationFilter.class));
        var encoder = cfg.passwordEncoder();
        var uds = cfg.userDetailsService("u", "p", encoder);

        UserDetails user = uds.loadUserByUsername("u");
        assertEquals("u", user.getUsername());
        assertTrue(encoder.matches("p", user.getPassword()));
    }

    @Test
    void corsConfigurationSourceHasExpectedOriginsAndMethods() {
        SecurityConfig cfg = new SecurityConfig(Mockito.mock(JwtAuthenticationFilter.class));
        var src = cfg.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        CorsConfiguration config = src.getCorsConfiguration(request);

        assertNotNull(config);
        assertTrue(config.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(config.getAllowedMethods().contains("GET"));
        assertTrue(config.getAllowedHeaders().contains("*"));
    }
}
