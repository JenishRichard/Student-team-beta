package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.service.BookingService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RefreshScope   //ADD (for dynamic refresh)
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    //ADD (inject value from config server)
    @Value("${custom.message}")
    private String message;

    public BookingController(BookingService service) {
        this.service = service;
    }

    //ADD (NEW endpoint for testing config server)
    @GetMapping("/config-test")
    public String configTest() {
        return message;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return service.getAllBookings();
    }

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return service.createBooking(booking);
    }

    @PutMapping("/{id}/cancel")
    public Booking cancelBooking(@PathVariable Long id) {
        return service.cancelBooking(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable Long id) {
        service.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }

    @GetMapping("/availability")
    public Map<String, Boolean> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam String timeRange) {

        boolean available = service.isRoomAvailable(roomId, timeRange);

        return Map.of("available", available);
    }
}