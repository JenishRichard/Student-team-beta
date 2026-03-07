ALTER TABLE bookings
    ADD COLUMN booking_time VARCHAR(20) NOT NULL DEFAULT '09:00-10:00' AFTER booking_date;

DROP INDEX uk_bookings_room_date_status ON bookings;

CREATE UNIQUE INDEX uk_bookings_room_date_time_status
    ON bookings (room_id, booking_date, booking_time, status);
