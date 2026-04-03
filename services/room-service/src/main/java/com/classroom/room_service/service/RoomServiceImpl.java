package com.classroom.room_service.service;

import com.classroom.room_service.entity.Room;
import com.classroom.room_service.exception.ResourceNotFoundException;
import com.classroom.room_service.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService{
    private static final String ROOM_NOT_FOUND_MESSAGE = "Room not found";

    private final RoomRepository roomRepository;

    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND_MESSAGE));
    }

    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(Long id, Room room) {

        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND_MESSAGE));

        existing.setRoomNumber(room.getRoomNumber());
        existing.setBuilding(room.getBuilding());
        existing.setCapacity(room.getCapacity());
        existing.setType(room.getType());
        existing.setAvailable(room.getAvailable());

        return roomRepository.save(existing);
    }

    @Override
    public void deleteRoom(Long id) {

        Room existing = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND_MESSAGE));

        roomRepository.delete(existing);
    }
    
    @Override
    public List<Room> filterRooms(String roomNumber, String building, String type) {

        List<Room> rooms = roomRepository.findAll();

        return rooms.stream()
                .filter(r -> roomNumber == null || r.getRoomNumber().equalsIgnoreCase(roomNumber))
                .filter(r -> building == null || r.getBuilding().equalsIgnoreCase(building))
                .filter(r -> type == null || r.getType().equalsIgnoreCase(type))
                .toList();
    }

	@Override
	public String getRoomDetails(Long roomId, String token) {
		return null;
	}
}
