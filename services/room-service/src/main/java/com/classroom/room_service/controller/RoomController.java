package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.service.RoomService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RefreshScope   
@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

   
    @Value("${custom.message}")
    private String message;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/config-test")
    public String configTest() {
        return message;
    }

    // GET /rooms
    @GetMapping
    public ResponseEntity<List<Room>> getRooms(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type) {

        List<Room> rooms = roomService.filterRooms(roomNumber, building, type);
        return ResponseEntity.ok(rooms);
    }

    // GET /rooms/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(room);
    }

    // POST /rooms
    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        Room saved = roomService.createRoom(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // PUT /rooms/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room room) {
        Room updated = roomService.updateRoom(id, room);
        return ResponseEntity.ok(updated);
    }

    // DELETE /rooms/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}