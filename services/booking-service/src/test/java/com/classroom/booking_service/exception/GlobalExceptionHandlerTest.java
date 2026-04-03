package com.classroom.booking_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    @Test
    void testHandleBookingConflict() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BookingConflictException ex =
                new BookingConflictException("Booking conflict occurred");

        String response = handler.handleBookingConflict(ex);

        assertEquals("Booking conflict occurred", response);
    }

    @Test
    void testBookingConflictExceptionMessage() {

        BookingConflictException ex =
                new BookingConflictException("Room already booked");

        assertEquals("Room already booked", ex.getMessage());
    }

    @Test
    void testHandleIllegalArgument() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        IllegalArgumentException ex =
                new IllegalArgumentException("Invalid input provided");

        String response = handler.handleIllegalArgument(ex);

        assertEquals("Invalid input provided", response);
    }
}