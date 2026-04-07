package com.classroom.booking_service.service;

import com.classroom.booking_service.dto.BookingStatusResponse;
import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void testGetAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(new Booking()));

        List<Booking> bookings = bookingService.getAllBookings();

        assertEquals(1, bookings.size());
    }

    @Test
    void testGetBookingById() {

        Booking booking = new Booking();
        booking.setId(1L); 

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));

        Booking result = bookingService.getBookingById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId()); 
    }
    @Test
    void testGetBookingByIdNotFound() {

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.getBookingById(1L)
        );
    }
    @Test
    void testCreateBooking() {
        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingDate(LocalDate.now());
        booking.setBookingTime("10:00-12:00");

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                101L, booking.getBookingDate(), BookingStatus.CONFIRMED))
                .thenReturn(List.of());

        when(bookingRepository.save(any())).thenReturn(booking);

        Booking result = bookingService.createBooking(booking);

        assertNotNull(result);
        verify(bookingRepository).save(any());
    }

    @Test
    void testCreateBookingConflict() {
        Booking existing = new Booking();
        existing.setRoomId(101L);
        existing.setBookingDate(LocalDate.now());
        existing.setBookingTime("10:00-12:00");
        existing.setStatus(BookingStatus.CONFIRMED);

        Booking newBooking = new Booking();
        newBooking.setRoomId(101L);
        newBooking.setBookingDate(LocalDate.now());
        newBooking.setBookingTime("11:00-13:00");

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                101L, newBooking.getBookingDate(), BookingStatus.CONFIRMED))
                .thenReturn(List.of(existing));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(newBooking));
    }

    @Test
    void testCancelBooking() {
        Booking booking = new Booking();

        when(bookingRepository.findById(1L))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any()))
                .thenReturn(booking);

        Booking result = bookingService.cancelBooking(1L);

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void testCancelBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.cancelBooking(1L));
    }

    @Test
    void testDeleteBooking() {
        Booking booking = new Booking();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(1L);

        verify(bookingRepository).deleteById(1L);
    }

    @Test
    void testDeleteBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.deleteBooking(1L));
    }

    @Test
    void testRoomAvailable() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("room exists");

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                eq(101L), any(LocalDate.class), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of());

        boolean result = bookingService.isRoomAvailable(101L, "10:00-12:00");

        assertTrue(result);
    }

    @Test
    void testRoomNotAvailable() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("room exists");

        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingTime("10:00-12:00");
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                eq(101L), any(LocalDate.class), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of(booking));

        boolean result = bookingService.isRoomAvailable(101L, "11:00-13:00");

        assertFalse(result);
    }

    @Test
    void testRoomDoesNotExist() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(HttpClientErrorException.NotFound.create(
                        HttpStatus.NOT_FOUND, "Not Found", null, null, null));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(999L, "10:00-12:00"));
    }

    @Test
    void testInvalidTimeRange() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("room exists");

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(101L, "invalid-range"));
    }

    @Test
    void testStartAfterEnd() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("room exists");

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(101L, "14:00-10:00"));
    }

    @Test
    void testAvailabilityFallback() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> bookingService.availabilityFallback(1L, "10:00-12:00", new RuntimeException()));

        assertEquals("Room service unavailable", ex.getMessage());
    }

    @Test
    void testGetRoomBookingStatusBooked() {
        Booking booking = new Booking();
        booking.setRoomId(101L);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                101L, LocalDate.now(), BookingStatus.CONFIRMED))
                .thenReturn(List.of(booking));

        BookingStatusResponse response = bookingService.getRoomBookingStatus(101L);

        assertTrue(response.booked());
        assertEquals("BOOKED", response.bookingStatus());
    }

    @Test
    void testGetRoomBookingStatusAvailable() {
        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                101L, LocalDate.now(), BookingStatus.CONFIRMED))
                .thenReturn(List.of());

        BookingStatusResponse response = bookingService.getRoomBookingStatus(101L);

        assertFalse(response.booked());
        assertEquals("AVAILABLE", response.bookingStatus());
    }

    @Test
    void testGetRoomDetailsSuccess() {
        ResponseEntity<String> response = new ResponseEntity<>("room data", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(response);

        CompletableFuture<String> result = bookingService.getRoomDetails(2L, "Bearer token");

        assertEquals("room data", result.join());
    }

    @Test
    void testFallbackRoomService() {
        CompletableFuture<String> result =
                bookingService.fallbackRoomService(2L, "Bearer token", new RuntimeException());

        assertEquals("Room service is slow or unavailable. Please try again later.", result.join());
    }
}
