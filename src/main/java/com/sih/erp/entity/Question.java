package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    // For a multiple-choice question
    @ElementCollection
    private List<String> options;

    @Column(nullable = false)
    private int correctOptionIndex; // e.g., 0, 1, 2, 3
}