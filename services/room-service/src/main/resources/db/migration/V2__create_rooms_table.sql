CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_number VARCHAR(255) NOT NULL,
    building VARCHAR(255),
    capacity INT,
    type VARCHAR(255),
    available BIT(1),
    PRIMARY KEY (id),
    CONSTRAINT uk_rooms_room_number UNIQUE (room_number)
);
