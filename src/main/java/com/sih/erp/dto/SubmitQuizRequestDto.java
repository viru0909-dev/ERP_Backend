package com.sih.erp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class SubmitQuizRequestDto {

    // A map where the Key is the questionId and the Value is the selected option index (e.g., 0, 1, 2)
    @NotEmpty
    private Map<UUID, Integer> answers;

}