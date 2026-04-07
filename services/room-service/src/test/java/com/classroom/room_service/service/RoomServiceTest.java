package com.classroom.room_service.service;

import com.classroom.room_service.dto.BookingStatusResponse;
import com.classroom.room_service.dto.RoomDetailsResponse;
import com.classroom.room_service.entity.Room;
import com.classroom.room_service.exception.ResourceNotFoundException;
import com.classroom.room_service.repository.RoomRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RestTemplate restTemplate;

    private RoomServiceImpl roomService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        roomService = new RoomServiceImpl(roomRepository, restTemplate, 0, "http://localhost:8083");
    }

    private Room createRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setBuilding("Main Block");
        room.setCapacity(40);
        room.setType("LECTURE");
        room.setAvailable(true);
        return room;
    }

    // getAllRooms
    @Test
    void getAllRooms_shouldReturnList() {

        Room room = createRoom();

        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = roomService.getAllRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("A101");
    }

    // getRoomById success
    @Test
    void getRoomById_shouldReturnRoom() {

        Room room = createRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        Room result = roomService.getRoomById(1L);

        assertThat(result.getRoomNumber()).isEqualTo("A101");
    }

    // getRoomById not found
    @Test
    void getRoomById_shouldThrowException() {

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room not found");
    }

    // createRoom
    @Test
    void createRoom_shouldSaveAndReturnRoom() {

        Room room = createRoom();

        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Room result = roomService.createRoom(room);

        assertThat(result.getRoomNumber()).isEqualTo("A101");
    }

    // updateRoom success
    @Test
    void updateRoom_shouldUpdateRoom() {

        Room existing = createRoom();
        Room updated = createRoom();
        updated.setCapacity(50);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roomRepository.save(any(Room.class))).thenReturn(existing);

        Room result = roomService.updateRoom(1L, updated);

        assertThat(result.getCapacity()).isEqualTo(50);
    }

    // updateRoom not found
    @Test
    void updateRoom_shouldThrowException() {

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        Room room = createRoom();

        assertThatThrownBy(() -> roomService.updateRoom(1L, room))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // deleteRoom success
    @Test
    void deleteRoom_shouldDeleteRoom() {

        Room room = createRoom();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(room);
    }

    // deleteRoom not found
    @Test
    void deleteRoom_shouldThrowException() {

        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.deleteRoom(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // filterRooms
    @Test
    void filterRooms_shouldReturnFilteredRooms() {

        Room room = createRoom();

        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<Room> result = roomService.filterRooms("A101", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomNumber()).isEqualTo("A101");
    }

    @Test
    void getRoomDetails_shouldReturnDetails() {
        Room room = createRoom();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(BookingStatusResponse.class)
        )).thenReturn(ResponseEntity.ok(new BookingStatusResponse(1L, true, "BOOKED")));

        CompletableFuture<RoomDetailsResponse> result = roomService.getRoomDetails(1L, "Bearer token");

        assertThat(result.join().bookingStatus()).isEqualTo("BOOKED");
        assertThat(result.join().booked()).isTrue();
        assertThat(result.join().roomNumber()).isEqualTo("A101");
    }

    @Test
    void fallbackRoomDetails_shouldReturnFallbackMessage() {
        Room room = createRoom();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        CompletableFuture<RoomDetailsResponse> result =
                roomService.fallbackRoomDetails(1L, "Bearer token", new RuntimeException("boom"));

        assertThat(result.join().bookingStatus()).isEqualTo("UNKNOWN");
        assertThat(result.join().bookingMessage()).isEqualTo("Booking service unavailable");
        assertThat(result.join().roomNumber()).isEqualTo("A101");
    }
}
