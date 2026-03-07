package com.classroom.booking_service.repository;

import com.classroom.booking_service.entity.BookingIdentity;
import com.classroom.booking_service.entity.ParticipantDirectory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantDirectoryRepository extends JpaRepository<ParticipantDirectory, Long> {
    List<ParticipantDirectory> findByIdentity(BookingIdentity identity);

    boolean existsByEmailAndIdentity(String email, BookingIdentity identity);
    boolean existsByParticipantIdAndIdentity(String participantId, BookingIdentity identity);
    ParticipantDirectory findByEmailAndIdentity(String email, BookingIdentity identity);

    long deleteByEmailIgnoreCaseAndIdentity(String email, BookingIdentity identity);
}
