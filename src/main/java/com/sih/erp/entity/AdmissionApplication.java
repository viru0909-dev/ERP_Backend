package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class AdmissionApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID applicationId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false, unique = true)
    private String applicantEmail;

    private String contactNumber;

    @Column(columnDefinition = "TEXT")
    private String previousEducationDetails;

    // We will skip file uploads for now
    // private String photoPath;
    // private String documentsPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @CreationTimestamp
    private LocalDateTime appliedAt;

    // The class the student is applying for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applying_class_id", nullable = false)
    private SchoolClass applyingClass;

    // This is the single, correct field for the staff member who reviews/processes the application
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_staff_id")
    private User reviewedBy;

    private LocalDateTime reviewedAt;

    // Link to the actual User account once accepted and created
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id")
    private User studentUser;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean wantsHostel;

}