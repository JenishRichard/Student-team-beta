package com.classroom.booking_service.service;

import com.classroom.booking_service.client.RoomServiceClient;
import com.classroom.booking_service.dto.BookingStatusResponse;
import com.classroom.booking_service.dto.RoomResponse;
import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomServiceClient roomServiceClient; 

    @InjectMocks
    private BookingService bookingService;

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(new Booking()));

        assertEquals(1, bookingService.getAllBookings().size());
    }

    @Test
    void testGetBookingById() {
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertEquals(1L, bookingService.getBookingById(1L).getId());
    }

    @Test
    void testGetBookingByIdNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.getBookingById(1L));
    }

    @Test
    void testCreateBooking() {
        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingTime("10:00-12:00");

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of());

        when(bookingRepository.save(any())).thenReturn(booking);

        assertNotNull(bookingService.createBooking(booking));
    }

    @Test
    void testCreateBookingConflict() {
        Booking existing = new Booking();
        existing.setBookingTime("10:00-12:00");
        existing.setStatus(BookingStatus.CONFIRMED);

        Booking newBooking = new Booking();
        newBooking.setBookingTime("11:00-13:00");
        newBooking.setRoomId(101L);
        newBooking.setBookingDate(LocalDate.now());

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of(existing));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(newBooking));
    }

    @Test
    void testCancelBooking() {
        Booking booking = new Booking();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking result = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void testDeleteBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(new Booking()));

        bookingService.deleteBooking(1L);

        verify(bookingRepository).deleteById(1L);
    }

    @Test
    void testRoomAvailable() {

        when(roomServiceClient.getRoomById(eq(101L), any()))
                .thenReturn(new RoomResponse());

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of());

        assertTrue(bookingService.isRoomAvailable(101L, "10:00-12:00"));
    }

    @Test
    void testRoomNotAvailable() {

        when(roomServiceClient.getRoomById(eq(101L), any()))
                .thenReturn(new RoomResponse());

        Booking booking = new Booking();
        booking.setBookingTime("10:00-12:00");
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        assertFalse(bookingService.isRoomAvailable(101L, "11:00-13:00"));
    }

    @Test
    void testRoomDoesNotExist() {

        when(roomServiceClient.getRoomById(eq(999L), any()))
                .thenThrow(new RuntimeException());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(999L, "10:00-12:00"));
    }

    @Test
    void testGetRoomBookingStatus() {

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of(new Booking()));

        BookingStatusResponse response = bookingService.getRoomBookingStatus(101L);

        assertTrue(response.booked());
    }

    @Test
    void testGetRoomDetails() {

        when(roomServiceClient.getRoomById(eq(2L), any()))
                .thenReturn(new RoomResponse());

        CompletableFuture<String> result =
                bookingService.getRoomDetails(2L, "Bearer token");

        assertNotNull(result.join());
    }

    @Test
    void testFallbackRoomService() {
        CompletableFuture<String> result =
                bookingService.fallbackRoomService(2L, "token", new RuntimeException());

        assertEquals(
                "Room service is slow or unavailable. Please try again later.",
                result.join()
        );
    }
}