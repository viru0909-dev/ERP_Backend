package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class XPTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID transactionId;

    // Which user earned the XP?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private String reason; // e.g., "Completed 'Java Basics' Quiz", "Answered a forum question"

    @CreationTimestamp
    private LocalDateTime timestamp;
}