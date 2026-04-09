package com.classroom.booking_service.service;

import com.classroom.booking_service.dto.BookingStatusResponse;
import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.entity.TimeRange;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.classroom.booking_service.exception.RoomServiceUnavailableException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.classroom.booking_service.client.RoomServiceClient;
import com.classroom.booking_service.dto.BookingWithRoomResponse;
import com.classroom.booking_service.dto.RoomResponse;
import com.classroom.booking_service.exception.ResourceNotFoundException;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final String BOOKING_NOT_FOUND = "Booking not found";
    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;
    private final RoomServiceClient roomServiceClient;

    public BookingService(BookingRepository bookingRepository, RestTemplate restTemplate, RoomServiceClient roomServiceClient) {
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
         this.roomServiceClient = roomServiceClient;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND));
    }
    public Booking createBooking(Booking booking) {
        List<Booking> existingBookings =
                bookingRepository.findByRoomIdAndBookingDateAndStatus(
                        booking.getRoomId(),
                        booking.getBookingDate(),
                        BookingStatus.CONFIRMED
                );

        TimeRange requestedRange = parseRange(booking.getBookingTime());

        for (Booking existing : existingBookings) {
            TimeRange existingRange = parseRange(existing.getBookingTime());

            if (overlaps(existingRange, requestedRange)) {
                throw new BookingConflictException(
                        "Room " + booking.getRoomId()
                                + " is already booked for "
                                + booking.getBookingTime()
                );
            }
        }

        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND));

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND));

        bookingRepository.deleteById(id);
    }

    public BookingStatusResponse getRoomBookingStatus(Long roomId) {
        boolean booked = !bookingRepository.findByRoomIdAndBookingDateAndStatus(
                roomId,
                LocalDate.now(),
                BookingStatus.CONFIRMED
        ).isEmpty();

        return new BookingStatusResponse(roomId, booked, booked ? "BOOKED" : "AVAILABLE");
    }


    @Retry(name = "roomService")
    @CircuitBreaker(name = "roomService", fallbackMethod = "availabilityFallback")
    public boolean isRoomAvailable(Long roomId, String range) {


        String url = "http://localhost:8081/rooms/" + roomId;

        try {
            restTemplate.getForObject(url, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException("Room does not exist");
        }


        TimeRange requestedRange = parseRange(range);

        List<Booking> bookings =
                bookingRepository.findByRoomIdAndBookingDateAndStatus(
                        roomId,
                        LocalDate.now(),
                        BookingStatus.CONFIRMED
                );

        for (Booking booking : bookings) {
            TimeRange existingRange = parseRange(booking.getBookingTime());

            if (overlaps(existingRange, requestedRange)) {
                return false;
            }
        }

        return true;
    }

    public boolean availabilityFallback(Long roomId, String range, Exception ex) {
        log.error("Room service unavailable for roomId={} and range={}", roomId, range, ex);
        throw new RoomServiceUnavailableException("Room service unavailable");
    }

 
    @Retry(name = "roomService")
    @CircuitBreaker(name = "roomService", fallbackMethod = "fallbackRoomService")
    @TimeLimiter(name = "roomService")
    public CompletableFuture<String> getRoomDetails(Long roomId, String token) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Calling room-service for roomId={}", roomId);
            log.info("Forwarding token: {}", token);

            String url = "http://localhost:8081/rooms/" + roomId;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            return response.getBody();
        });
    }

    public CompletableFuture<String> fallbackRoomService(Long roomId, String token, Exception ex) {
    	log.error("Room service failed for roomId={}, authHeaderPresent={}",
    	        roomId,
    	        token != null && !token.isBlank(),
    	        ex);

        return CompletableFuture.completedFuture(
                "Room service is slow or unavailable. Please try again later."
        );
    }
    private TimeRange parseRange(String range) {
        try {
            String[] parts = range.split("-");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid time range format");
            }

            LocalTime start = LocalTime.parse(parts[0]);
            LocalTime end = LocalTime.parse(parts[1]);

            if (!start.isBefore(end)) {
                throw new IllegalArgumentException("Start must be before end");
            }

            return new TimeRange(start, end);

        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid time format HH:mm-HH:mm");
        }
    }

    private boolean overlaps(TimeRange a, TimeRange b) {
        return a.start().isBefore(b.end()) && b.start().isBefore(a.end());
    }

    public BookingWithRoomResponse getBookingWithRoom(Long id, String token) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Using Feign instead of RestTemplate
        RoomResponse room = roomServiceClient.getRoomById(booking.getRoomId(), token);

        BookingWithRoomResponse response = new BookingWithRoomResponse();

        response.setBookedBy(booking.getBookedBy());
        response.setId(booking.getId());
        response.setRoomId(booking.getRoomId());
        response.setBookedByIdentity(booking.getBookedByIdentity());
        response.setBookingDate(booking.getBookingDate().toString());
        response.setBookingTime(booking.getBookingTime());
        response.setStatus(booking.getStatus());
        response.setRoom(room);

        return response;
    }
}
