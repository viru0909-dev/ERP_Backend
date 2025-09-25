package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseDto {
    // Information about the class
    private UUID classId;
    private String gradeLevel;
    private String section;

    // All subjects taught in this class
    private Set<SubjectDto> subjects;
}