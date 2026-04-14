package com.classroom.booking_service.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    private JwtAuthenticationFilter filter;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
        // clear security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_forNonBookingUris() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/actuator/health");

        assertTrue(filter.shouldNotFilter(req));
    }

    @Test
    void missingAuthorizationHeader_returns401AndNoChainCall() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/bookings");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("Missing Bearer token"));
        assertNull(chain.getRequest());
    }

    @Test
    void invalidToken_returns401AndNoChainCall() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/bookings");
        req.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        doThrow(new JwtException("invalid")).when(jwtService).validate("bad-token");

        filter.doFilter(req, res, chain);

        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("Invalid token"));
        assertNull(chain.getRequest());
    }

    @Test
    void whenResponseAlreadyCommitted_writeUnauthorizedDoesNothing() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/bookings");
        MockHttpServletResponse res = new MockHttpServletResponse();
        // mark as committed so writeUnauthorized returns early
        res.setCommitted(true);
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        // nothing should have been written and chain not invoked
        assertEquals(0, res.getContentAsString().length());
        assertNull(chain.getRequest());
    }

    @Test
    void validToken_setsSecurityContextAndContinuesChain() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/bookings");
        req.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        DefaultClaims claims = new DefaultClaims();
        claims.setSubject("booking-user");

        when(jwtService.validate("valid-token")).thenReturn(claims);

        filter.doFilter(req, res, chain);

        assertNotNull(chain.getRequest());
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("booking-user", auth.getPrincipal());
    }
}
