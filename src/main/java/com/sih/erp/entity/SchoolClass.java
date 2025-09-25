package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter; // Import Getter
import lombok.Setter; // Import Setter
import java.util.UUID;

@Entity
@Data
@Getter // <-- ADD THIS ANNOTATION
@Setter // <-- ADD THIS ANNOTATION
public class SchoolClass {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID classId;

    @Column(nullable = false)
    private String gradeLevel;

    @Column(nullable = false)
    private String section;

    // You can remove the @TableGenerator and @UniqueConstraint for now
    // if it's not immediately needed, to simplify the entity.
    private int durationInYears;
    private String feeStructure; // e.g., "Approx. 1,00,000 INR per year"
    private String highestPackage; // e.g., "12 LPA"
    @Column(columnDefinition = "TEXT")
    private String programHighlights;

    private int sectionCapacity;

}
