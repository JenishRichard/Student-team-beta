package com.classroom.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.classroom.booking_service.dto.RoomResponse;

@FeignClient(name = "room-service", url = "${room.service.url}")
public interface RoomServiceClient {

    @GetMapping("/rooms/{id}")
    RoomResponse getRoomById(
        @PathVariable("id") Long id,
        @RequestHeader("Authorization") String token
    );
}
