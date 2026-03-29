package com.classroom.booking_service.service;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.entity.TimeRange;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.repository.BookingRepository;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
@RefreshScope
@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
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
                .orElseThrow(() -> new RuntimeException("Booking not found"));

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