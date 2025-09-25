package com.sih.erp.dto;

import com.sih.erp.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TeacherProfileDto {
    // Basic user info
    private UUID userId;
    private String fullName;
    private String email;
    private String contactNumber;
    private Role role;

    // Teacher-specific info
    private Set<SubjectDto> taughtSubjects;
    private Set<SchoolClassDto> taughtClasses;
}
