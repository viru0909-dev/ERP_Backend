package com.sih.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateQuizRequestDto {

    @NotBlank(message = "Quiz title cannot be blank")
    private String title;

    @NotNull(message = "Subject ID cannot be null")
    private UUID subjectId;

    @NotEmpty(message = "A quiz must have at least one question")
    private List<QuestionDto> questions;

    // Inner class to represent a single question within the request
    @Data
    public static class QuestionDto {
        @NotBlank(message = "Question text cannot be blank")
        private String questionText;

        @NotEmpty(message = "Question must have options")
        private List<String> options;

        @NotNull(message = "Correct option index must be provided")
        private Integer correctOptionIndex;
    }
}