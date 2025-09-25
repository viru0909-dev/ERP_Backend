package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AssignmentDto {
    private UUID assignmentId;
    private String title;
    private String instructions;
    private LocalDateTime dueDate;
    private LocalDateTime assignedAt;
    private String createdBy; // Teacher's name
}