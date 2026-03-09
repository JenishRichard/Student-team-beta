package com.classroom.booking_service.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void testBookingEntity() {

        Booking booking = new Booking();

        booking.setRoomId(101L);
        booking.setBookedBy("teacher@test.com");
        booking.setBookedByIdentity(BookingIdentity.TEACHER);
        booking.setBookingDate(LocalDate.of(2026,3,9));
        booking.setBookingTime("10:00-12:00");
        booking.setStatus(BookingStatus.CONFIRMED);

        assertEquals(101L, booking.getRoomId());
        assertEquals("teacher@test.com", booking.getBookedBy());
        assertEquals(BookingIdentity.TEACHER, booking.getBookedByIdentity());
        assertEquals(LocalDate.of(2026,3,9), booking.getBookingDate());
        assertEquals("10:00-12:00", booking.getBookingTime());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void testParticipantDirectoryEntity() {

        ParticipantDirectory participant = new ParticipantDirectory();

        participant.setEmail("student@test.com");
        participant.setParticipantId("P001");
        participant.setIdentity(BookingIdentity.STUDENT);

        assertEquals("student@test.com", participant.getEmail());
        assertEquals("P001", participant.getParticipantId());
        assertEquals(BookingIdentity.STUDENT, participant.getIdentity());
    }

    @Test
    void testTimeRangeRecord() {

        TimeRange range = new TimeRange(
                LocalTime.of(10,0),
                LocalTime.of(12,0)
        );

        assertEquals(LocalTime.of(10,0), range.start());
        assertEquals(LocalTime.of(12,0), range.end());
    }
}