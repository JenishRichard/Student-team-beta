package com.classroom.api_gateway.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private JwtAuthFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthFilter(jwtService);
        chain = mock(FilterChain.class);
    }

    // ✅ 1. Allow /auth endpoint (no token required)
    @Test
    void shouldAllowAuthEndpointWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtService, never()).validate(any());
    }

    // ✅ 2. Allow non-protected endpoints
    @Test
    void shouldAllowNonProtectedEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/public");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    // ✅ 3. OPTIONS (CORS preflight)
    @Test
    void shouldHandleOptionsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/rooms");
        request.addHeader("Origin", "http://localhost:5173");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
        verify(chain, never()).doFilter(request, response);
    }

    // ❌ 4. Missing token for protected endpoint
    @Test
    void shouldReturn401WhenTokenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing Bearer token"));
        verify(chain, never()).doFilter(request, response);
    }

    // ❌ 5. Invalid token
    @Test
    void shouldReturn401WhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bookings");
        request.addHeader("Authorization", "Bearer bad-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        doThrow(new JwtException("Invalid")).when(jwtService).validate("bad-token");

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid token"));
        verify(chain, never()).doFilter(request, response);
    }

    // ✅ 6. Valid token
    @Test
    void shouldAllowWhenTokenValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms");
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        doNothing().when(jwtService).validate("valid-token");

        filter.doFilter(request, response, chain);

        verify(jwtService).validate("valid-token");
        verify(chain).doFilter(request, response);
    }

    // ✅ 7. CORS headers always applied
    @Test
    void shouldApplyCorsHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/rooms");
        request.addHeader("Origin", "http://localhost:5173");

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, chain);

        assertEquals("http://localhost:5173", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));
    }
}