package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private BookingService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllBookings_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setRoomId(1L);
        booking.setBookedBy("Jenish");
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        Mockito.when(service.getAllBookings())
                .thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomId").value(1));
    }

    @Test
    void createBooking_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setRoomId(1L);
        booking.setBookedBy("Jenish");
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        Mockito.when(service.createBooking(any()))
                .thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(1));
    }

    @Test
    void cancelBooking_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);

        Mockito.when(service.cancelBooking(1L))
                .thenReturn(booking);

        mockMvc.perform(put("/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}