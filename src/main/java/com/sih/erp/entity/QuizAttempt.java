package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID attemptId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score; // e.g., 8

    @Column(nullable = false)
    private int totalQuestions; // e.g., 10

    @CreationTimestamp
    private LocalDateTime completedAt;
}