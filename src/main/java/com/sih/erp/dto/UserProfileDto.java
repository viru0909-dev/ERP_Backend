package com.sih.erp.dto;

import com.sih.erp.entity.AcademicStatus;
import com.sih.erp.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A Data Transfer Object representing the publicly safe information of a user.
 * This is sent to the frontend after a user logs in.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private UUID userId;
    private String fullName;
    private String email;
    private String contactNumber;
    private Role role;

    private String branch;
    private String password;

    private String rollNumber;
    private String mentorName;
    private boolean feePaid;
    private AcademicStatus academicStatus;



}

