package com.classroom.booking_service.entity;

import java.time.LocalTime;

public record TimeRange(LocalTime start, LocalTime end) {
}