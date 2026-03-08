package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.exception.ResourceNotFoundException;
import com.classroom.room_service.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

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

    // GET /rooms
    @Test
    void getAllRooms_shouldReturnOk() throws Exception {

        Room room = createRoom();

        when(roomService.filterRooms(null,null,null)).thenReturn(List.of(room));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }

    // GET /rooms?roomNumber=A101
    @Test
    void filterRooms_shouldReturnFilteredRooms() throws Exception {

        Room room = createRoom();

        when(roomService.filterRooms("A101", null, null))
                .thenReturn(List.of(room));

        mockMvc.perform(get("/rooms")
                        .param("roomNumber","A101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }

    // GET /rooms/{id}
    @Test
    void getRoomById_shouldReturnRoom() throws Exception {

        Room room = createRoom();

        when(roomService.getRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("A101"));
    }

    // GET /rooms/{id} invalid
    @Test
    void getRoomById_shouldReturn404() throws Exception {

        when(roomService.getRoomById(1L))
                .thenThrow(new ResourceNotFoundException("Room not found"));

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isNotFound());
    }

    // POST /rooms
    @Test
    void createRoom_shouldReturn201() throws Exception {

        Room room = createRoom();

        when(roomService.createRoom(any(Room.class))).thenReturn(room);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("A101"));
    }

    // PUT /rooms/{id}
    @Test
    void updateRoom_shouldReturnUpdatedRoom() throws Exception {

        Room room = createRoom();

        when(roomService.updateRoom(anyLong(), any(Room.class))).thenReturn(room);

        mockMvc.perform(put("/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(room)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("A101"));
    }

    // DELETE /rooms/{id}
    @Test
    void deleteRoom_shouldReturn204() throws Exception {

        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNoContent());
    }

    // DELETE invalid
    @Test
    void deleteRoom_shouldReturn404() throws Exception {

        doThrow(new ResourceNotFoundException("Room not found"))
                .when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNotFound());
    }
}