package com.sih.erp.dto;

import com.sih.erp.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    private String contactNumber;

    @NotNull(message = "Role must be provided")
    private Role role;

    // These fields are used for both Teacher and Student registration
    private Set<UUID> subjectIds;
    private Set<UUID> classIds;

    private UUID mentorId;
}