package com.sih.erp.dto;

import com.sih.erp.entity.Question;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionForStudentDto {
    private UUID questionId;
    private String questionText;
    private List<String> options;

    public QuestionForStudentDto(Question question) {
        this.questionId = question.getQuestionId();
        this.questionText = question.getQuestionText();
        this.options = question.getOptions();
    }
}