package com.classroom.booking_service.controller;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.entity.ParticipantDirectory;
import com.classroom.booking_service.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
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
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        boolean deleted = service.deleteBooking(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participants")
    public List<ParticipantDirectory> getParticipants(@RequestParam BookingIdentity identity) {
        return service.getParticipants(identity);
    }

    public record AddParticipantRequest(String participantId, String email, BookingIdentity identity) {}

    @PostMapping("/participants")
    public ParticipantDirectory addParticipant(@RequestBody AddParticipantRequest request) {
        return service.addParticipant(request.participantId(), request.email(), request.identity());
    }

    @DeleteMapping("/participants")
    public ResponseEntity<Void> deleteParticipantBookings(
            @RequestParam String bookedBy,
            @RequestParam BookingIdentity identity) {
        long deleted = service.deleteParticipantBookings(bookedBy, identity);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
