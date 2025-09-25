package com.sih.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateAssignmentRequest {
    @NotBlank
    private String title;
    private String instructions;
    private LocalDateTime dueDate;
}