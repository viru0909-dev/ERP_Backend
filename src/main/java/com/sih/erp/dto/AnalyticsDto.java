// Create new file: src/main/java/com/sih/erp/dto/AnalyticsDto.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AnalyticsDto {
    private long totalStudents;
    private long totalTeachers;
    private Map<String, Long> studentsPerClass;
    private long hostelResidents;
    // We will add more stats like fee payments here later
}