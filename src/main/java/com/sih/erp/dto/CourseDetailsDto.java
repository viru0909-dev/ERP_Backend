package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDto {
    private List<CourseModuleDto> modules;
    private List<AssignmentDto> assignments;
    private List<QuizDto> quizzes;
}