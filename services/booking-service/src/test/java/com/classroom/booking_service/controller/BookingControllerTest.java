package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
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
    void testDeleteBooking() throws Exception {

        Mockito.when(bookingService.deleteBooking(1L)).thenReturn(true);

        mockMvc.perform(delete("/bookings/1"))
                .andExpect(status().isNoContent());
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
    void testGetParticipants() throws Exception {

        Mockito.when(bookingService.getParticipants(Mockito.any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/participants")
                .param("identity", BookingIdentity.TEACHER.name()))
                .andExpect(status().isOk());
    }

    @Test
    void testAddParticipant() throws Exception {

        BookingController.AddParticipantRequest request =
                new BookingController.AddParticipantRequest(
                        "P001",
                        "teacher@test.com",
                        BookingIdentity.TEACHER
                );

        Mockito.when(bookingService.addParticipant(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any()))
                .thenReturn(null);

        mockMvc.perform(post("/bookings/participants")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteParticipantBookings() throws Exception {

        Mockito.when(bookingService.deleteParticipantBookings(
                Mockito.anyString(),
                Mockito.any()))
                .thenReturn(1L);

        mockMvc.perform(delete("/bookings/participants")
                .param("bookedBy", "teacher@test.com")
                .param("identity", BookingIdentity.TEACHER.name()))
                .andExpect(status().isNoContent());
    }
}