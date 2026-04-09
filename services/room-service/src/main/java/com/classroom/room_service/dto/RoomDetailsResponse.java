package com.classroom.room_service.dto;

import com.classroom.room_service.entity.Room;

public record RoomDetailsResponse(
        Long id,
        String roomNumber,
        String building,
        Integer capacity,
        String type,
        Boolean available,
        boolean booked,
        String bookingStatus,
        String bookingMessage
) {
    public static RoomDetailsResponse from(Room room, BookingStatusResponse bookingStatus) {
        return new RoomDetailsResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getBuilding(),
                room.getCapacity(),
                room.getType(),
                room.getAvailable(),
                bookingStatus.booked(),
                bookingStatus.bookingStatus(),
                null
        );
    }

    public static RoomDetailsResponse fallback(Room room, String bookingMessage) {
        return new RoomDetailsResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getBuilding(),
                room.getCapacity(),
                room.getType(),
                room.getAvailable(),
                false,
                "UNKNOWN",
                bookingMessage
        );
    }
}
