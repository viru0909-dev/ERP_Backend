package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID classroomId;

    @Column(nullable = false, unique = true)
    private String roomNumber; // e.g., "C-101", "Lab-A"

    private int capacity;
}