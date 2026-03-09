package com.classroom.booking_service.service;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.repository.BookingRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void testRoomAvailable() {

        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingTime("14:00-15:00");

        when(bookingRepository.findAll())
                .thenReturn(List.of(booking));

        boolean result = bookingService.isRoomAvailable(101L, "10:00-12:00");

        assertTrue(result);
    }

    @Test
    void testRoomNotAvailable() {

        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingTime("10:00-12:00");

        when(bookingRepository.findAll())
                .thenReturn(List.of(booking));

        boolean result = bookingService.isRoomAvailable(101L, "11:00-13:00");

        assertFalse(result);
    }

    @Test
    void testDifferentRoomAvailable() {

        Booking booking = new Booking();
        booking.setRoomId(200L);
        booking.setBookingTime("10:00-12:00");

        when(bookingRepository.findAll())
                .thenReturn(List.of(booking));

        boolean result = bookingService.isRoomAvailable(101L, "10:00-12:00");

        assertTrue(result);
    }

    @Test
    void testInvalidTimeRange() {

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(101L, "invalid-range")
        );
    }
    @Test
    void testGetAllBookings() {

        when(bookingRepository.findAll()).thenReturn(List.of(new Booking()));

        List<Booking> bookings = bookingService.getAllBookings();

        assertEquals(1, bookings.size());
    }

    @Test
    void testCreateBooking() {

    	Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingTime("10:00-12:00");

        when(bookingRepository.save(any()))
                .thenReturn(booking);

        Booking result = bookingService.createBooking(booking);

        assertNotNull(result);
        verify(bookingRepository).save(any());
    }

    @Test
    void testCancelBooking() {

        Booking booking = new Booking();

        when(bookingRepository.findById(1L))
                .thenReturn(java.util.Optional.of(booking));

        when(bookingRepository.save(any()))
                .thenReturn(booking);

        Booking result = bookingService.cancelBooking(1L);

        assertNotNull(result);
    }

    @Test
    void testDeleteBooking() {

        when(bookingRepository.existsById(1L)).thenReturn(true);

        bookingService.deleteBooking(1L);

        verify(bookingRepository).deleteById(1L);
    }
  
    @Test
    void testStartAfterEnd() {

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(101L, "14:00-10:00")
        );
    }
}