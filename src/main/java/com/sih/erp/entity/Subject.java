package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID subjectId;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "Mathematics", "Physics"
}