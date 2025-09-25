package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CourseModuleDto {
    private UUID moduleId;
    private String title;
    private String description;
    private String fileUrl;
    private LocalDateTime createdAt;
    private String createdBy; // Teacher's name
}