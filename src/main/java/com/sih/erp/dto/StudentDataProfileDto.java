package com.sih.erp.dto;

import com.sih.erp.entity.AcademicStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDataProfileDto {
    // Basic Info from User.java
    private UUID studentId;
    private String fullName;
    private String rollNumber;
    private AcademicStatus academicStatus;
    private boolean feePaid;

    // Aggregated Data
    private double attendancePercentage; // You'll need a service to calculate this
    private double averageExamScore;     // You'll need a service to calculate this

    // Data from your existing RiskAnalytics service
    private double riskProbability;
}