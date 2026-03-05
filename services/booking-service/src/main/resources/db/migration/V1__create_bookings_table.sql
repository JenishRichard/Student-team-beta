CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT,
    booked_by VARCHAR(255),
    booking_date DATE,
    status VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_bookings_status CHECK (status IN ('CONFIRMED', 'CANCELLED'))
);

CREATE UNIQUE INDEX uk_bookings_room_date_status
    ON bookings (room_id, booking_date, status);
