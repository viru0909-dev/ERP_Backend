package com.sih.erp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AdmissionRequestDto {
    @NotBlank
    private String applicantName;

    @NotBlank
    @Email
    private String applicantEmail;

    private String contactNumber;

    private String previousEducationDetails;

    @NotNull
    private UUID applyingClassId;

    private boolean wantsHostel;
}