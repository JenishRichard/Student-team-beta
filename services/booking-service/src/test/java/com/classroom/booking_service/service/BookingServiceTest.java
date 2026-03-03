package com.classroom.booking_service.service;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    @InjectMocks
    private BookingService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBooking_success() {
        Booking booking = new Booking();
        booking.setRoomId(1L);
        booking.setBookingDate(LocalDate.now());

        when(repository.existsByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), eq(BookingStatus.CONFIRMED)))
                .thenReturn(false);

        when(repository.save(any())).thenReturn(booking);

        Booking result = service.createBooking(booking);

        assertNotNull(result);
        verify(repository, times(1)).save(booking);
    }

    @Test
    void createBooking_shouldThrowException_whenAlreadyConfirmed() {
        Booking booking = new Booking();
        booking.setRoomId(1L);
        booking.setBookingDate(LocalDate.now());

        when(repository.existsByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), eq(BookingStatus.CONFIRMED)))
                .thenReturn(true);

        assertThrows(BookingConflictException.class,
                () -> service.createBooking(booking));
    }

    @Test
    void cancelBooking_success() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);

        when(repository.findById(1L)).thenReturn(Optional.of(booking));
        when(repository.save(any())).thenReturn(booking);

        Booking result = service.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelBooking_shouldThrowException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.cancelBooking(1L));
    }
}