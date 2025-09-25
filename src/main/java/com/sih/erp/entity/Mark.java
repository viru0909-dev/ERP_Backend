package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Mark {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) private UUID id;
    @ManyToOne
    private User student;
    @ManyToOne private Subject subject;
    private Double marksObtained;
    private Double totalMarks = 100.0; // Default total marks
    @Enumerated(EnumType.STRING) private ExamType examType; // e.g., MID_TERM, FINAL
    @ManyToOne private User uploadedBy; // The teacher
}
