package com.classroom.booking_service.service;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;
import com.classroom.booking_service.repository.ParticipantDirectoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @Mock
    private BookingRepository repository;

    @Mock
    private ParticipantDirectoryRepository participantDirectoryRepository;

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
        booking.setBookedBy("teacher1@classroom.com");
        booking.setBookingDate(LocalDate.now());
        booking.setBookingTime("09:00-10:00");

        when(repository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of());
        when(participantDirectoryRepository.findByEmailAndIdentity(anyString(), any()))
                .thenReturn(null);
        when(participantDirectoryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.save(any())).thenReturn(booking);

        Booking result = service.createBooking(booking);

        assertNotNull(result);
        verify(repository, times(1)).save(booking);
    }

    @Test
    void createBooking_shouldThrowException_whenOverlappingSlotExists() {
        Booking requested = new Booking();
        requested.setRoomId(1L);
        requested.setBookedBy("teacher2@classroom.com");
        requested.setBookingDate(LocalDate.now());
        requested.setBookingTime("09:00-11:00");

        Booking existing = new Booking();
        existing.setRoomId(1L);
        existing.setBookingDate(requested.getBookingDate());
        existing.setBookingTime("09:30-10:00");
        existing.setStatus(BookingStatus.CONFIRMED);

        when(repository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of(existing));
        when(participantDirectoryRepository.findByEmailAndIdentity(anyString(), any()))
                .thenReturn(null);
        when(participantDirectoryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(BookingConflictException.class, () -> service.createBooking(requested));
    }

    @Test
    void createBooking_shouldAllow_whenNonOverlappingSlotExists() {
        Booking requested = new Booking();
        requested.setRoomId(1L);
        requested.setBookedBy("teacher3@classroom.com");
        requested.setBookingDate(LocalDate.now());
        requested.setBookingTime("10:00-11:00");

        Booking existing = new Booking();
        existing.setRoomId(1L);
        existing.setBookingDate(requested.getBookingDate());
        existing.setBookingTime("09:00-10:00");
        existing.setStatus(BookingStatus.CONFIRMED);

        when(repository.findByRoomIdAndBookingDateAndStatus(
                anyLong(), any(), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of(existing));
        when(participantDirectoryRepository.findByEmailAndIdentity(anyString(), any()))
                .thenReturn(null);
        when(participantDirectoryRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(repository.save(any())).thenReturn(requested);

        Booking result = service.createBooking(requested);

        assertNotNull(result);
        verify(repository, times(1)).save(requested);
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
