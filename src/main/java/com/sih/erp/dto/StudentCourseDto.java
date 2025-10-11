package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseDto {
    private UUID classId;
    private String gradeLevel;
    private String section;
    private Set<SubjectDto> subjects;

    // --- ADD THESE TWO NEW FIELDS ---
    private List<CourseModuleDto> modules;
    private List<AssignmentDto> assignments;
    private List<QuizListDto> quizzes;

}