package com.classroom.booking_service.repository;

import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByRoomIdAndBookingDateAndStatus(
            Long roomId,
            LocalDate bookingDate,
            BookingStatus status
    );

    long deleteByBookedByIgnoreCaseAndBookedByIdentity(String bookedBy, BookingIdentity bookedByIdentity);
}
