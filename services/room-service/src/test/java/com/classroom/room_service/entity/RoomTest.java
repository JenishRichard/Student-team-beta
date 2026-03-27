package com.classroom.room_service.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoomTest {

    @Test
    void shouldSetAndGetFieldsCorrectly_andDefaultAvailableTrue() {
        Room room = new Room();

        // default
        assertThat(room.getAvailable()).isTrue();

        // set values
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setBuilding("Main Block");
        room.setCapacity(A);
        room.setType("LECTURE");
        room.setAvailable(false);

        // verify
        assertThat(room.getId()).isEqualTo(1L);
        assertThat(room.getRoomNumber()).isEqualTo("A101");
        assertThat(room.getBuilding()).isEqualTo("Main Block");
        assertThat(room.getCapacity()).isEqualTo(40);
        assertThat(room.getType()).isEqualTo("LECTURE");
        assertThat(room.getAvailable()).isFalse();
    }
}