package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.exception.ResourceNotFoundException;
import com.classroom.room_service.security.JwtService;
import com.classroom.room_service.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtService jwtService;

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

    @Test
    void getAllRooms_shouldReturnOk() throws Exception {

        Room room = createRoom();

        when(roomService.filterRooms(null,null,null)).thenReturn(List.of(room));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }

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

    @Test
    void getRoomById_shouldReturnRoom() throws Exception {

        Room room = createRoom();

        when(roomService.getRoomById(1L)).thenReturn(room);

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("A101"));
    }

    @Test
    void getRoomDetails_shouldReturnDetails() throws Exception {

        when(roomService.getRoomDetails(1L, "Bearer token"))
                .thenReturn(CompletableFuture.completedFuture(new com.classroom.room_service.dto.RoomDetailsResponse(
                        1L, "A101", "Main Block", 40, "LECTURE", true, true, "BOOKED", null
                )));

        MvcResult result = mockMvc.perform(get("/rooms/room-details/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("A101"))
                .andExpect(jsonPath("$.bookingStatus").value("BOOKED"))
                .andExpect(jsonPath("$.booked").value(true));
    }

    @Test
    void getRoomById_shouldReturn404() throws Exception {

        when(roomService.getRoomById(1L))
                .thenThrow(new ResourceNotFoundException("Room not found"));

        mockMvc.perform(get("/rooms/1"))
                .andExpect(status().isNotFound());
    }

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

    @Test
    void deleteRoom_shouldReturn204() throws Exception {

        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRoom_shouldReturn404() throws Exception {

        doThrow(new ResourceNotFoundException("Room not found"))
                .when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/rooms/1"))
                .andExpect(status().isNotFound());
    }
}
