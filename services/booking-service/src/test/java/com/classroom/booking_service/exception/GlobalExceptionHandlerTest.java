package com.classroom.booking_service.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {


  
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