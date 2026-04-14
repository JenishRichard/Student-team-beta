package com.classroom.booking_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import feign.FeignException;
import static org.mockito.Mockito.mock;

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

    @Test
    void testHandleResourceNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ResourceNotFoundException ex = new ResourceNotFoundException("Not here");
        String response = handler.handleResourceNotFound(ex);
        assertEquals("Not here", response);
    }

    @Test
    void testHandleFeignNotFound() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        FeignException.NotFound feign = mock(FeignException.NotFound.class);
        String response = handler.handleFeignNotFound(feign);
        assertEquals("Room not found", response);
    }

    @Test
    void testHandleGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception ex = new Exception("oops");
        String response = handler.handleGenericException(ex);
        assertEquals("Internal server error", response);
    }
}