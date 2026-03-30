package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(BookingController.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false"
})
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

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
    void testCreateBooking() throws Exception {

        Booking booking = new Booking();

        Mockito.when(bookingService.createBooking(Mockito.any()))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk());
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

        Mockito.when(bookingService.isRoomAvailable(101L,"10:00-12:00"))
                .thenReturn(true);

        mockMvc.perform(get("/bookings/availability")
                .param("roomId","101")
                .param("timeRange","10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void testCheckAvailabilityNotAvailable() throws Exception {

        Mockito.when(bookingService.isRoomAvailable(101L,"10:00-12:00"))
                .thenReturn(false);

        mockMvc.perform(get("/bookings/availability")
                .param("roomId","101")
                .param("timeRange","10:00-12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}