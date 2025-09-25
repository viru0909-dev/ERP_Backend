package com.sih.erp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateModuleRequest {
    @NotBlank
    private String title;
    private String description;
    private String fileUrl;
}