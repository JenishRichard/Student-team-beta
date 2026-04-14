package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.security.JwtService;
import com.classroom.booking_service.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
<<<<<<< HEAD
import org.springframework.test.context.ActiveProfiles;
=======
>>>>>>> a9f9104 (Test Cases fix and coverage)
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

<<<<<<< HEAD

@ActiveProfiles("test")
@WebMvcTest(controllers = BookingController.class)

@AutoConfigureMockMvc(addFilters = false)
=======
@ExtendWith(MockitoExtension.class)
>>>>>>> a9f9104 (Test Cases fix and coverage)
class BookingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookingService bookingService;

    @Mock
    private JwtService jwtService;

    @Mock
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        BookingController controller = new BookingController(bookingService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

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