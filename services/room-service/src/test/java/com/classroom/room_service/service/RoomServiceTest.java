package com.classroom.room_service.service;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    @Test
    void getAllRooms_shouldReturnList() {
        Room r = new Room();
        r.setId(1L);
        r.setRoomNumber("B201");

        when(roomRepository.findAll()).thenReturn(List.of(r));

        List<Room> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("B201");
    }

    @Test
    void createRoom_shouldSaveAndReturnRoom() {
        Room input = new Room();
        input.setRoomNumber("C101");

        Room saved = new Room();
        saved.setId(99L);
        saved.setRoomNumber("C101");

        when(roomRepository.save(any(Room.class))).thenReturn(saved);

        Room result = roomService.createRoom(input);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getRoomNumber()).isEqualTo("C101");
    }
}