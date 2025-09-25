package com.sih.erp.dto;

import com.sih.erp.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionApplicationDto {
    private UUID applicationId;
    private String applicantName;
    private String applicantEmail;
    private String contactNumber;
    private String previousEducationDetails;
    private String programName;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private String reviewedByStaffName;
    private UserProfileDto studentUser;
}