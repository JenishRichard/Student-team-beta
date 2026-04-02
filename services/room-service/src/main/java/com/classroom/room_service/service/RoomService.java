package com.classroom.room_service.service;

import com.classroom.room_service.entity.Room;

import java.util.List;

public interface RoomService {

    List<Room> getAllRooms();
    
    List<Room> filterRooms(String roomNumber, String building, String type);

    Room getRoomById(Long id);

    Room createRoom(Room room);

    Room updateRoom(Long id, Room room);

    void deleteRoom(Long id);

	String getRoomDetails(Long roomId, String token);


}
