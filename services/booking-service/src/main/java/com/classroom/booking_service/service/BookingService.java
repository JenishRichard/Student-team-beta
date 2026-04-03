package com.classroom.booking_service.service;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RestTemplate restTemplate;

    public BookingService(BookingRepository bookingRepository, RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.restTemplate = restTemplate;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
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
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found");
        }

        bookingRepository.deleteById(id);
    }

    public boolean isRoomAvailable(Long roomId, String range) {
        TimeRange requestedRange = parseRange(range);
        List<Booking> bookings = bookingRepository.findAll();

        for (Booking booking : bookings) {
            if (booking.getRoomId().equals(roomId)) {
                TimeRange existingRange = parseRange(booking.getBookingTime());

                if (overlaps(existingRange, requestedRange)) {
                    return false;
                }
            }
        }

        return true;
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
    public CompletableFuture<String> fallbackRoomService(Long roomId, Exception ex) {
        log.error("Room service failed or timed out for roomId={}", roomId, ex);
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
}