package com.classroom.booking_service.exception;

public class RoomServiceUnavailableException extends RuntimeException {

    public RoomServiceUnavailableException(String message) {
        super(message);
    }
}