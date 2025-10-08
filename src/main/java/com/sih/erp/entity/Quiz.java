package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID quizId;

    @Column(nullable = false)
    private String title;

    // A quiz belongs to one specific subject (e.g., "Core Java")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    // A quiz can have many questions
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    // Who created this quiz?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
}