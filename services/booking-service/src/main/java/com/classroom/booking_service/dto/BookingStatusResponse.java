package com.classroom.booking_service.dto;

public record BookingStatusResponse(Long roomId, boolean booked, String bookingStatus) {
}
