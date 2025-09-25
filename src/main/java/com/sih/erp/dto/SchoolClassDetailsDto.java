package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassDetailsDto {
    private UUID classId;
    private String gradeLevel;
    private String section;
    private int durationInYears;
    private String feeStructure;
    private String highestPackage;
    private String programHighlights;
    private Set<SubjectDto> subjects;
}