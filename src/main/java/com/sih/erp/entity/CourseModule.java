package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class CourseModule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID moduleId;

    @Column(nullable = false)
    private String title;

    @Lob // For longer text descriptions
    @Column(columnDefinition = "TEXT")
    private String description;

    // This could be a link to a Google Drive file, a PDF uploaded to storage, etc.
    private String fileUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // --- Relationships ---

    // Many modules belong to one specific subject within one specific class.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    // The teacher who created this module
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User createdBy;
}
