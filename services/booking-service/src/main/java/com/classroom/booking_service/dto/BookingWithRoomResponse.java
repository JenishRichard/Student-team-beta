package com.classroom.booking_service.dto;

import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.entity.BookingStatus;

public class BookingWithRoomResponse {

    private String bookedBy;
    private Long id;
    private Long roomId;
    private BookingIdentity bookedByIdentity;
    private String bookingDate;
    private String bookingTime;
    private BookingStatus status;
    private RoomResponse room;

    public BookingWithRoomResponse() {
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public BookingIdentity getBookedByIdentity() {
        return bookedByIdentity;
    }

    public void setBookedByIdentity(BookingIdentity bookingIdentity) {
        this.bookedByIdentity = bookingIdentity;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(String bookingTime) {
        this.bookingTime = bookingTime;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus bookingStatus) {
        this.status = bookingStatus;
    }

    public RoomResponse getRoom() {
        return room;
    }

    public void setRoom(RoomResponse room) {
        this.room = room;
    }
}