package com.sih.erp.dto;

import com.sih.erp.entity.QuizAttempt;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QuizAttemptResultDto {
    private UUID attemptId;
    private UUID quizId;
    private int score;
    private int totalQuestions;
    private double percentage;
    private LocalDateTime completedAt;

    public QuizAttemptResultDto(QuizAttempt attempt) {
        this.attemptId = attempt.getAttemptId();
        this.quizId = attempt.getQuiz().getQuizId();
        this.score = attempt.getScore();
        this.totalQuestions = attempt.getTotalQuestions();
        this.percentage = (double) attempt.getScore() / attempt.getTotalQuestions() * 100;
        this.completedAt = attempt.getCompletedAt();
    }
}