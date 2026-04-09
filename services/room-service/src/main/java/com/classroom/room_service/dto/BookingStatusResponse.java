package com.classroom.room_service.dto;

public record BookingStatusResponse(Long roomId, boolean booked, String bookingStatus) {
}
