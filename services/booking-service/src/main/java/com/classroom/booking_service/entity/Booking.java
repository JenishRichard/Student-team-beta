package com.classroom.booking_service.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
	    name = "bookings",
	    uniqueConstraints = @UniqueConstraint(
	            columnNames = {"room_id", "booking_date", "booking_time", "status"}
	    )
	)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    private String bookedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingIdentity bookedByIdentity = BookingIdentity.TEACHER;

    private LocalDate bookingDate;

    @Column(nullable = false, length = 20)
    private String bookingTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.CONFIRMED;

    //---- Getters & Setters -----

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public BookingIdentity getBookedByIdentity() {
        return bookedByIdentity;
    }

    public void setBookedByIdentity(BookingIdentity bookedByIdentity) {
        this.bookedByIdentity = bookedByIdentity;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
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

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
