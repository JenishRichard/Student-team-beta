ALTER TABLE bookings
    ADD COLUMN booked_by_identity VARCHAR(16) NOT NULL DEFAULT 'TEACHER' AFTER booked_by;
