package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.security.JwtAuthenticationFilter;
import com.classroom.room_service.security.JwtService;
import com.classroom.room_service.security.SecurityConfig;
import com.classroom.room_service.service.RoomService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RoomSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldReturn401WhenRoomsEndpointHasNoToken() throws Exception {
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Missing Bearer token"));
    }

    @Test
    void shouldReturn401WhenRoomsEndpointHasInvalidToken() throws Exception {
        when(jwtService.validate("bad-token")).thenThrow(new JwtException("bad token"));

        mockMvc.perform(get("/rooms").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid token"));
    }

    @Test
    void shouldAllowRoomsEndpointWithValidToken() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("A101");
        room.setBuilding("Main Block");
        room.setCapacity(40);
        room.setType("LECTURE");
        room.setAvailable(true);

        when(roomService.filterRooms(null, null, null)).thenReturn(List.of(room));
        when(jwtService.validate("valid-token")).thenReturn(new io.jsonwebtoken.impl.DefaultClaims());

        mockMvc.perform(get("/rooms").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("A101"));
    }
}
