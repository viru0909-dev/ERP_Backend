// In src/main/java/com/sih/erp/entity/HostelRegistration.java
package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class HostelRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status;

    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    private Double feeAmount; // <-- ADD THIS FIELD

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean feePaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room assignedRoom;
}