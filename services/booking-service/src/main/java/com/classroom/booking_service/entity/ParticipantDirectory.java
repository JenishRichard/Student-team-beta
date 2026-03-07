package com.classroom.booking_service.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "participant_directory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"identity", "email"}),
                @UniqueConstraint(columnNames = {"identity", "participant_id"})
        }
)
public class ParticipantDirectory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "participant_id", nullable = false, length = 64)
    private String participantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BookingIdentity identity;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getParticipantId() {
        return participantId;
    }

    public void setParticipantId(String participantId) {
        this.participantId = participantId;
    }

    public BookingIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(BookingIdentity identity) {
        this.identity = identity;
    }
}
