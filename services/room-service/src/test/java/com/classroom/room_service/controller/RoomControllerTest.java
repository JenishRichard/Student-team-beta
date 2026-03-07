package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @Test
    void getAll_shouldReturnOkAndJsonArray() throws Exception {
        Room r1 = new Room();
        r1.setId(1L);
        r1.setRoomNumber("A101");
        r1.setBuilding("Main Block");
        r1.setCapacity(40);
        r1.setType("LECTURE");
        r1.setAvailable(true);

        when(roomService.getAllRooms()).thenReturn(List.of(r1));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }

    @Test
    void create_shouldSaveAndReturnCreatedRoom() throws Exception {
        Room input = new Room();
        input.setRoomNumber("C301");
        input.setBuilding("Science Block");
        input.setCapacity(55);
        input.setType("SEMINAR");
        input.setAvailable(true);

        Room saved = new Room();
        saved.setId(10L);
        saved.setRoomNumber("C301");
        saved.setBuilding("Science Block");
        saved.setCapacity(55);
        saved.setType("SEMINAR");
        saved.setAvailable(true);

        when(roomService.createRoom(any(Room.class))).thenReturn(saved);

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.roomNumber").value("C301"));
    }
}