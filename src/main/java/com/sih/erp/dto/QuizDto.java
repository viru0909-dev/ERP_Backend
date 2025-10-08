package com.sih.erp.dto;

import com.sih.erp.entity.Quiz;
import lombok.Data;

import java.util.UUID;

@Data
public class QuizDto {
    private UUID quizId;
    private String title;
    private String subjectName;
    private String createdBy;

    // A handy constructor to easily convert a Quiz entity to a QuizDto
    public QuizDto(Quiz quiz) {
        this.quizId = quiz.getQuizId();
        this.title = quiz.getTitle();
        this.subjectName = quiz.getSubject().getName(); // Assuming Subject entity has a getName() method
        this.createdBy = quiz.getCreatedBy().getFullName();
    }
}