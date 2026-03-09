package com.classroom.booking_service.service;

import com.classroom.booking_service.entity.*;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;
import com.classroom.booking_service.repository.ParticipantDirectoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ParticipantDirectoryRepository participantDirectoryRepository;

    @InjectMocks
    private BookingService bookingService;

    private Booking booking;

    @BeforeEach
    void setup() {

        booking = new Booking();
        booking.setRoomId(10L);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingTime("10:00-11:00");
        booking.setBookedBy("teacher@test.com");
        booking.setBookedByIdentity(BookingIdentity.TEACHER);
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Test
    void testGetAllBookings() {

        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        List<Booking> result = bookingService.getAllBookings();

        assertEquals(1, result.size());
        verify(bookingRepository).findAll();
    }

    @Test
    void testCreateBookingSuccess() {

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        when(bookingRepository.save(any())).thenReturn(booking);

        Booking saved = bookingService.createBooking(booking);

        assertNotNull(saved);
        verify(bookingRepository).save(any());
    }

    @Test
    void testCreateBookingConflict() {

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), any()))
                .thenReturn(List.of(booking));

        assertThrows(
                BookingConflictException.class,
                () -> bookingService.createBooking(booking)
        );
    }

    @Test
    void testCancelBooking() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        when(bookingRepository.save(any()))
                .thenReturn(booking);

        Booking cancelled = bookingService.cancelBooking(1L);

        assertNotNull(cancelled);
        verify(bookingRepository).save(any());
    }

    @Test
    void testDeleteBooking() {

        when(bookingRepository.existsById(1L)).thenReturn(true);

        bookingService.deleteBooking(1L);

        verify(bookingRepository).deleteById(1L);
    }

    @Test
    void testDeleteBookingNotFound() {

        when(bookingRepository.existsById(1L)).thenReturn(false);

        bookingService.deleteBooking(1L);

        verify(bookingRepository, never()).deleteById(1L);
    }

    @Test
    void testGetParticipants() {

        when(participantDirectoryRepository.findByIdentity(BookingIdentity.TEACHER))
                .thenReturn(List.of());

        List<?> result = bookingService.getParticipants(BookingIdentity.TEACHER);

        assertNotNull(result);
    }

    @Test
    void testAddParticipant() {

        when(participantDirectoryRepository.save(any())).thenReturn(null);

        bookingService.addParticipant(
                "teacher@test.com",
                "P001",
                BookingIdentity.TEACHER
        );

        verify(participantDirectoryRepository).save(any());
    }

    @Test
    void testDeleteParticipantBookings() {

        when(bookingRepository.deleteByBookedByIgnoreCaseAndBookedByIdentity(
                anyString(), any()))
                .thenReturn(1L);

        when(participantDirectoryRepository.deleteByEmailIgnoreCaseAndIdentity(
                anyString(), any()))
                .thenReturn(1L);

        long result = bookingService.deleteParticipantBookings(
                "teacher@test.com",
                BookingIdentity.TEACHER
        );

        assertTrue(result > 0);
    }

    @Test
    void testDeleteParticipantBookingsInvalidEmail() {

        assertThrows(
                BookingConflictException.class,
                () -> bookingService.deleteParticipantBookings(
                        null,
                        BookingIdentity.TEACHER
                )
        );
    }
}