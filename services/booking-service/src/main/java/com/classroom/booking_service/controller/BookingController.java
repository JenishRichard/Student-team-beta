package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.service.BookingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    // GET /bookings
    @GetMapping
    public List<Booking> getAllBookings() {
        return service.getAllBookings();
    }

    // POST /bookings
    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return service.createBooking(booking);
    }

    // PUT /bookings/{id}/cancel
    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return service.cancelBooking(id);
    }

    // DELETE /bookings/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        service.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    // GET /bookings/availability
    @GetMapping("/availability")
    public Map<String, Boolean> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam String timeRange) {

        boolean available = service.isRoomAvailable(roomId, timeRange);

        return Map.of("available", available);
    }
}