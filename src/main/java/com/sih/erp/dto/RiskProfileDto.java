// Create new file: src/main/java/com/sih/erp/dto/RiskProfileDto.java
package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskProfileDto {
    private UUID studentId;
    private String studentName;
    private double riskProbability; // A value between 0.0 and 1.0
    private double attendancePercentage;
    private double lastExamScore;
    private boolean isFeePaid;


}