package com.sih.erp.dto;

import com.sih.erp.entity.QuizAttempt;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QuizAttemptDto {
    private UUID attemptId;
    private String studentName;
    private int score;
    private int totalQuestions;
    private double percentage;
    private LocalDateTime completedAt;
    private int attemptNumber;

    public QuizAttemptDto(QuizAttempt attempt) {
        this.attemptId = attempt.getAttemptId();
        this.studentName = attempt.getStudent().getFullName();
        this.score = attempt.getScore();
        this.totalQuestions = attempt.getTotalQuestions();
        this.percentage = Math.round((double) attempt.getScore() / attempt.getTotalQuestions() * 100);
        this.completedAt = attempt.getCompletedAt();
        this.attemptNumber = attempt.getAttemptNumber();
    }
}