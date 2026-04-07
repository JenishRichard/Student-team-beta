package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.security.JwtService;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BookingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "management.endpoints.enabled-by-default=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllBookings() throws Exception {
        Booking booking = new Booking();

        Mockito.when(bookingService.getAllBookings())
                .thenReturn(List.of(booking));

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
        Booking booking = new Booking();

        Mockito.when(bookingService.cancelBooking(1L))
                .thenReturn(booking);

        mockMvc.perform(put("/bookings/1/cancel"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBooking() throws Exception {
        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testCheckAvailabilityAvailable() throws Exception {
        Mockito.when(bookingService.isRoomAvailable(101L, "10:00-12:00"))
                .thenReturn(true);

        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", "101")
                        .param("timeRange", "10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testCheckAvailabilityNotAvailable() throws Exception {
        Mockito.when(bookingService.isRoomAvailable(101L, "10:00-12:00"))
                .thenReturn(false);

        mockMvc.perform(get("/bookings/availability")
                        .param("roomId", "101")
                        .param("timeRange", "10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void testGetRoomDetails() throws Exception {
        Mockito.when(bookingService.getRoomDetails(2L, "Bearer token"))
                .thenReturn(CompletableFuture.completedFuture("room data"));

        mockMvc.perform(get("/bookings/room-details/2")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk());
    }
}
