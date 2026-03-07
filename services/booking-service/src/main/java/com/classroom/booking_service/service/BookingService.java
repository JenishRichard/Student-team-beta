package com.classroom.booking_service.service;
import com.classroom.booking_service.exception.BookingConflictException;
import com.classroom.booking_service.entity.Booking;
import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.entity.BookingStatus;
import com.classroom.booking_service.repository.BookingRepository;
import com.classroom.booking_service.entity.ParticipantDirectory;
import com.classroom.booking_service.repository.ParticipantDirectoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository repository;
    private final ParticipantDirectoryRepository participantDirectoryRepository;

    public BookingService(
            BookingRepository repository,
            ParticipantDirectoryRepository participantDirectoryRepository) {
        this.repository = repository;
        this.participantDirectoryRepository = participantDirectoryRepository;
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }

    public Booking createBooking(Booking booking) {
        if (booking.getBookedByIdentity() == null) {
            booking.setBookedByIdentity(BookingIdentity.TEACHER);
        }

        // Keep participant directory synced from booking activity.
        upsertParticipant(booking.getBookedBy(), booking.getBookedByIdentity());

        TimeRange requested = parseRange(booking.getBookingTime());

        List<Booking> confirmedSameDay = repository.findByRoomIdAndBookingDateAndStatus(
                booking.getRoomId(),
                booking.getBookingDate(),
                BookingStatus.CONFIRMED
        );

        for (Booking existing : confirmedSameDay) {
            TimeRange current = parseRange(existing.getBookingTime());
            if (overlaps(requested, current)) {
                throw new BookingConflictException("Room already booked for this date and overlapping time slot");
            }
        }

        return repository.save(booking);
    }

    @Transactional
    public long deleteParticipantBookings(String bookedBy, BookingIdentity identity) {
        String normalized = normalizeEmail(bookedBy);
        long deletedBookings = repository.deleteByBookedByIgnoreCaseAndBookedByIdentity(normalized, identity);
        long deletedParticipant = participantDirectoryRepository.deleteByEmailIgnoreCaseAndIdentity(normalized, identity);
        return deletedBookings + deletedParticipant;
    }

    public boolean deleteBooking(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    public List<ParticipantDirectory> getParticipants(BookingIdentity identity) {
        return participantDirectoryRepository.findByIdentity(identity);
    }

    public ParticipantDirectory addParticipant(String participantId, String email, BookingIdentity identity) {
        String normalizedId = normalizeParticipantId(participantId);
        String normalized = normalizeEmail(email);
        if (participantDirectoryRepository.existsByEmailAndIdentity(normalized, identity)) {
            throw new BookingConflictException("Participant already exists");
        }
        if (participantDirectoryRepository.existsByParticipantIdAndIdentity(normalizedId, identity)) {
            throw new BookingConflictException("Participant ID already exists");
        }
        ParticipantDirectory participant = new ParticipantDirectory();
        participant.setParticipantId(normalizedId);
        participant.setEmail(normalized);
        participant.setIdentity(identity);
        return participantDirectoryRepository.save(participant);
    }

    public Booking cancelBooking(Long id) {

        Booking booking = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);

        return repository.save(booking);
    }

    private TimeRange parseRange(String bookingTime) {
        try {
            String[] parts = bookingTime.split("-");
            if (parts.length != 2) throw new IllegalArgumentException();

            LocalTime start = LocalTime.parse(parts[0].trim());
            LocalTime end = LocalTime.parse(parts[1].trim());

            if (!end.isAfter(start)) throw new IllegalArgumentException();
            return new TimeRange(start, end);
        } catch (Exception ex) {
            throw new BookingConflictException("Invalid booking time. Use HH:mm-HH:mm with end after start");
        }
    }

    private boolean overlaps(TimeRange first, TimeRange second) {
        return first.start.isBefore(second.end) && second.start.isBefore(first.end);
    }

    private void upsertParticipant(String email, BookingIdentity identity) {
        String normalized = normalizeEmail(email);
        ParticipantDirectory existing = participantDirectoryRepository.findByEmailAndIdentity(normalized, identity);
        if (existing != null) return;
        ParticipantDirectory participant = new ParticipantDirectory();
        participant.setParticipantId(generateParticipantId(identity));
        participant.setEmail(normalized);
        participant.setIdentity(identity);
        participantDirectoryRepository.save(participant);
    }

    private String normalizeEmail(String email) {
        if (email == null) throw new BookingConflictException("Email is required");
        String normalized = email.trim().toLowerCase();
        if (normalized.isEmpty()) throw new BookingConflictException("Email is required");
        return normalized;
    }

    private String normalizeParticipantId(String participantId) {
        if (participantId == null) throw new BookingConflictException("Participant ID is required");
        String normalized = participantId.trim().toUpperCase();
        if (normalized.isEmpty()) throw new BookingConflictException("Participant ID is required");
        return normalized;
    }

    private String generateParticipantId(BookingIdentity identity) {
        String prefix = identity == BookingIdentity.TEACHER ? "TCH-AUTO-" : "STD-AUTO-";
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private record TimeRange(LocalTime start, LocalTime end) {}
}
