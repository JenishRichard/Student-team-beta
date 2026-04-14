package com.classroom.booking_service.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsSimpleTest {

    @Test
    void bookingConflictExceptionStoresMessage() {
        BookingConflictException ex = new BookingConflictException("conf");
        assertEquals("conf", ex.getMessage());
    }

    @Test
    void resourceNotFoundExceptionStoresMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void roomServiceUnavailableExceptionStoresMessage() {
        RoomServiceUnavailableException ex = new RoomServiceUnavailableException("down");
        assertEquals("down", ex.getMessage());
    }
}
