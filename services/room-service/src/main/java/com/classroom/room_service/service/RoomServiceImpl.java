package com.classroom.room_service.service;

import com.classroom.room_service.dto.BookingStatusResponse;
import com.classroom.room_service.dto.RoomDetailsResponse;
import com.classroom.room_service.entity.Room;
import com.classroom.room_service.exception.ResourceNotFoundException;
import com.classroom.room_service.repository.RoomRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RoomServiceImpl implements RoomService{
    private static final String ROOM_NOT_FOUND_MESSAGE = "Room not found";
    private static final String ROOM_DETAILS_FALLBACK =
            "Booking service unavailable";
    private static final Logger log = LoggerFactory.getLogger(RoomServiceImpl.class);

    private final RoomRepository roomRepository;
    private final RestTemplate restTemplate;
    private final long delayMs;
    private final String bookingServiceBaseUrl;

    public RoomServiceImpl(RoomRepository roomRepository,
                           RestTemplate restTemplate,
                           @Value("${custom.delay-ms:0}") long delayMs,
                           @Value("${booking-service.base-url}") String bookingServiceBaseUrl) {
        this.roomRepository = roomRepository;
        this.restTemplate = restTemplate;
        this.delayMs = delayMs;
        this.bookingServiceBaseUrl = bookingServiceBaseUrl;
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
    @Retry(name = "roomServiceRetry", fallbackMethod = "fallbackRoomDetails")
    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "fallbackRoomDetails")
    @TimeLimiter(name = "roomServiceTimeout", fallbackMethod = "fallbackRoomDetails")
    public CompletableFuture<RoomDetailsResponse> getRoomDetails(Long roomId, String token) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND_MESSAGE));

        return CompletableFuture.supplyAsync(() -> {
            log.info("Fetching room details for roomId={}", roomId);
            simulateDelay();
            return RoomDetailsResponse.from(room, fetchBookingStatus(roomId, token));
        });
    }

    public CompletableFuture<RoomDetailsResponse> fallbackRoomDetails(Long roomId, String token, Throwable ex) {
        log.error("Fallback triggered for roomId={} because {}", roomId, ex.getMessage());

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(ROOM_NOT_FOUND_MESSAGE));

        return CompletableFuture.completedFuture(RoomDetailsResponse.fallback(room, ROOM_DETAILS_FALLBACK));
    }

    private void simulateDelay() {
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while simulating room service delay", ex);
        }
    }

    private BookingStatusResponse fetchBookingStatus(Long roomId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, token);

        ResponseEntity<BookingStatusResponse> response = restTemplate.exchange(
                bookingServiceBaseUrl + "/bookings/rooms/" + roomId + "/status",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                BookingStatusResponse.class
        );

        return response.getBody() == null
                ? new BookingStatusResponse(roomId, false, "UNKNOWN")
                : response.getBody();
    }
}
