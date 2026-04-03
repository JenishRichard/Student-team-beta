package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;

import com.classroom.room_service.service.RoomService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private static final Logger log = LoggerFactory.getLogger(RoomController.class);
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
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

    @GetMapping("/room-details/{roomId}")
    public String getRoomDetails(
            @PathVariable Long roomId,
            @RequestHeader("Authorization") String token) {
    	log.info("TOKEN IN CONTROLLER: {}", token);
        return roomService.getRoomDetails(roomId, token);
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
