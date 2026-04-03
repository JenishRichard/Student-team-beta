package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getAllBookings());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getBookingById(id));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createBooking(booking));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.cancelBooking(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        service.deleteBooking(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Booking deleted successfully");
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam String timeRange) {

        boolean available = service.isRoomAvailable(roomId, timeRange);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("available", available));
    }

    @GetMapping("/room-details/{roomId}")
    public ResponseEntity<CompletableFuture<String>> getRoomDetails(
            @PathVariable Long roomId,
            @RequestHeader("Authorization") String token) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getRoomDetails(roomId, token));
    }
}