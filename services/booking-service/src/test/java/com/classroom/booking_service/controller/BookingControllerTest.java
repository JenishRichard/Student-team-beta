package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.security.JwtAuthenticationFilter;
import com.classroom.booking_service.security.JwtService;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class, properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "management.endpoints.enabled-by-default=false",
        "jwt.secret=test-secret-key-at-least-32-characters",
        "jwt.expiration-ms=3600000"
})
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void testGetAllBookings() throws Exception {
        Mockito.when(bookingService.getAllBookings())
                .thenReturn(List.of(new Booking()));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBookingById() throws Exception {
        Booking booking = new Booking();
        booking.setRoomId(1L);

        Mockito.when(bookingService.getBookingById(1L))
                .thenReturn(booking);

        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRoomBookingStatus() throws Exception {
        Mockito.when(bookingService.getRoomBookingStatus(101L))
                .thenReturn(new com.classroom.booking_service.dto.BookingStatusResponse(101L, true, "BOOKED"));

        mockMvc.perform(get("/bookings/rooms/101/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(101))
                .andExpect(jsonPath("$.booked").value(true))
                .andExpect(jsonPath("$.bookingStatus").value("BOOKED"));
    }

    @Test
    void testCreateBooking() throws Exception {
        Booking booking = new Booking();

        Mockito.when(bookingService.createBooking(Mockito.any()))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCancelBooking() throws Exception {
        Mockito.when(bookingService.cancelBooking(1L))
                .thenReturn(new Booking());

        mockMvc.perform(put("/bookings/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBooking() throws Exception {
        Mockito.doNothing().when(bookingService).deleteBooking(1L);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckAvailabilityAvailable() throws Exception {
        Mockito.when(bookingService.isRoomAvailable(101L, "10:00-12:00"))
                .thenReturn(true);

        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", "101")
                        .param("range", "10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testCheckAvailabilityNotAvailable() throws Exception {
        Mockito.when(bookingService.isRoomAvailable(101L, "10:00-12:00"))
                .thenReturn(false);

        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", "101")
                        .param("range", "10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void testGetRoomDetails() throws Exception {
        Mockito.when(bookingService.getRoomDetails(2L, "Bearer token"))
                .thenReturn(CompletableFuture.completedFuture("room data"));

        mockMvc.perform(get("/bookings/room-details/2")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string("room data"));
    }
}