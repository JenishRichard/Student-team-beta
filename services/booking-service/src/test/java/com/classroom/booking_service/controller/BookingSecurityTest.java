package com.classroom.booking_service.controller;

import com.classroom.booking_service.client.RoomServiceClient;
import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.security.JwtAuthenticationFilter;
import com.classroom.booking_service.security.JwtService;
import com.classroom.booking_service.service.BookingService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
=======
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
>>>>>>> a9f9104 (Test Cases fix and coverage)
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

<<<<<<< HEAD
@ActiveProfiles("test")
@WebMvcTest(controllers = BookingController.class)

@Import(SecurityConfig.class)
=======
@ExtendWith(MockitoExtension.class)
>>>>>>> a9f9104 (Test Cases fix and coverage)
class BookingSecurityTest {

    private MockMvc mockMvc;

    @Mock
    private BookingService bookingService;

    @Mock
    private JwtService jwtService;

    @Mock
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Mock
    private RoomServiceClient roomServiceClient;

    @BeforeEach
    void setUp() {
        // Build controller with mocked service
        BookingController controller = new BookingController(bookingService);

        // Instantiate filter with mocked jwtService
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(jwtFilter)
                .build();
    }

    @Test
    void shouldReturn401WhenBookingsEndpointHasNoToken() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing Bearer token"));
    }

    @Test
    void shouldReturn401WhenBookingsEndpointHasInvalidToken() throws Exception {
        when(jwtService.validate("bad-token")).thenThrow(new JwtException("bad token"));

        mockMvc.perform(get("/bookings").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void shouldAllowBookingsEndpointWithValidToken() throws Exception {
        when(jwtService.validate("valid-token")).thenReturn(new DefaultClaims());
        when(bookingService.getAllBookings()).thenReturn(List.of(new Booking()));

        mockMvc.perform(get("/bookings").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowBookingStatusEndpointWithValidToken() throws Exception {
        when(jwtService.validate("valid-token")).thenReturn(new DefaultClaims());
        when(bookingService.getRoomBookingStatus(26L))
                .thenReturn(new com.classroom.booking_service.dto.BookingStatusResponse(26L, true, "BOOKED"));

        mockMvc.perform(get("/bookings/rooms/26/status").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingStatus").value("BOOKED"));
    }
}
