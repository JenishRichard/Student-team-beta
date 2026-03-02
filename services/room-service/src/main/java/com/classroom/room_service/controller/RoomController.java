package com.classroom.room_service.controller;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomRepository repository;

    @PostMapping
    public Room create(@RequestBody Room room) {
        return repository.save(room);
    }

    @GetMapping
    public List<Room> getAll() {
        return repository.findAll();
    }
}
