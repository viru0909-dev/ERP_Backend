package com.sih.erp.dto;

import com.sih.erp.entity.Quiz;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class QuizForStudentDto {
    private UUID quizId;
    private String title;
    private String subjectName;
    private List<QuestionForStudentDto> questions;

    public QuizForStudentDto(Quiz quiz) {
        this.quizId = quiz.getQuizId();
        this.title = quiz.getTitle();
        this.subjectName = quiz.getSubject().getName();
        this.questions = quiz.getQuestions().stream()
                .map(QuestionForStudentDto::new)
                .collect(Collectors.toList());
    }
}