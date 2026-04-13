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
import org.springframework.web.client.RestTemplate;
import com.classroom.booking_service.client.RoomServiceClient;
import com.classroom.booking_service.dto.BookingWithRoomResponse;
import com.classroom.booking_service.dto.RoomResponse;
import com.classroom.booking_service.entity.BookingIdentity;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RestTemplate restTemplate;
    
    @Mock
    private RoomServiceClient roomServiceClient;

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

        RoomResponse room = new RoomResponse();
        room.setId(101L);

        when(roomServiceClient.getRoomById(eq(101L), anyString()))
                .thenReturn(room);

        when(bookingRepository.findByRoomIdAndBookingDateAndStatus(
                eq(101L), any(LocalDate.class), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of());

        boolean result = bookingService.isRoomAvailable(101L, "10:00-12:00");

        assertTrue(result);
    }

    @Test
    void testRoomNotAvailable() {

        RoomResponse room = new RoomResponse();
        room.setId(101L);

        when(roomServiceClient.getRoomById(eq(101L), anyString()))
                .thenReturn(room);

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

        when(roomServiceClient.getRoomById(eq(999L), anyString()))
                .thenThrow(new IllegalArgumentException("Room not found"));

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(999L, "10:00-12:00"));
    }

    @Test
    void testInvalidTimeRange() {

        RoomResponse room = new RoomResponse();
        room.setId(101L);

        when(roomServiceClient.getRoomById(eq(101L), anyString()))
                .thenReturn(room);

        assertThrows(IllegalArgumentException.class,
                () -> bookingService.isRoomAvailable(101L, "invalid-range"));
    }

    @Test
    void testStartAfterEnd() {

        RoomResponse room = new RoomResponse();
        room.setId(101L);

        when(roomServiceClient.getRoomById(eq(101L), anyString()))
                .thenReturn(room);

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

        ResponseEntity<String> response =
                new ResponseEntity<>("room data", HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(response);

        CompletableFuture<String> result =
                bookingService.getRoomDetails(2L, "Bearer token");

        assertNotNull(result);
        assertEquals("room data", result.join());
    }
    
    @Test
    void testFallbackRoomService() {
        CompletableFuture<String> result =
                bookingService.fallbackRoomService(2L, "Bearer token", new RuntimeException());

        assertEquals("Room service is slow or unavailable. Please try again later.", result.join());
    }

    @Test
        void testGetBookingWithRoom_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoomId(101L);
        booking.setBookedBy("Sanket");
        booking.setBookedByIdentity(BookingIdentity.STUDENT);
        booking.setBookingDate(LocalDate.of(2026, 4, 13));
        booking.setBookingTime("10:00-11:00");
        booking.setStatus(BookingStatus.CONFIRMED);

        RoomResponse room = new RoomResponse();
        room.setId(101L);
        room.setRoomNumber("A101");
        room.setCapacity(40);
        room.setAvailable(true);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(roomServiceClient.getRoomById(101L, "Bearer token")).thenReturn(room);

        BookingWithRoomResponse response =
                bookingService.getBookingWithRoom(1L, "Bearer token");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getRoomId());
        assertEquals("Sanket", response.getBookedBy());
        assertEquals(BookingIdentity.STUDENT, response.getBookedByIdentity());
        assertEquals(LocalDate.of(2026, 4, 13).toString(), response.getBookingDate());
        assertEquals("10:00-11:00", response.getBookingTime());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertNotNull(response.getRoom());
        assertEquals(101L, response.getRoom().getId());
        assertEquals("A101", response.getRoom().getRoomNumber());

        verify(bookingRepository).findById(1L);
        verify(roomServiceClient).getRoomById(101L, "Bearer token");
    }
}
