package com.sih.erp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class HostelComplaint {
    @Id private UUID id;
    @ManyToOne private User student;
    private String title;
    @Lob private String description;
    @Enumerated(EnumType.STRING) private ComplaintStatus status; // OPEN, RESOLVED
    private LocalDateTime createdAt;
}