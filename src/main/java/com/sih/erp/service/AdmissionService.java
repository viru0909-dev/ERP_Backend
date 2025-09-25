package com.sih.erp.service;

import com.sih.erp.dto.AdmissionApplicationDto;
import com.sih.erp.dto.AdmissionRequestDto;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.dto.UserRegistrationRequest;
import com.sih.erp.entity.*;
import com.sih.erp.repository.AdmissionApplicationRepository;
import com.sih.erp.repository.SchoolClassRepository;
import com.sih.erp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdmissionService {

    @Autowired private AdmissionApplicationRepository applicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private SchoolClassRepository schoolClassRepository;

    @Transactional
    public AdmissionApplication submitApplication(AdmissionRequestDto request) {
        SchoolClass applyingClass = schoolClassRepository.findById(request.getApplyingClassId())
                .orElseThrow(() -> new RuntimeException("Class to apply for not found"));

        AdmissionApplication newApplication = new AdmissionApplication();
        newApplication.setApplicantName(request.getApplicantName());
        newApplication.setApplicantEmail(request.getApplicantEmail());
        newApplication.setContactNumber(request.getContactNumber());
        newApplication.setPreviousEducationDetails(request.getPreviousEducationDetails());
        newApplication.setApplyingClass(applyingClass);
        newApplication.setStatus(ApplicationStatus.PENDING);

        return applicationRepository.save(newApplication);
    }

    @Transactional(readOnly = true)
    public List<AdmissionApplicationDto> getPendingApplications() {
        return applicationRepository.findByStatusOrderByAppliedAtDesc(ApplicationStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdmissionApplicationDto approveApplication(UUID applicationId, String staffEmail) {
        AdmissionApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        User staffMember = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Staff user not found: " + staffEmail));

        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(staffMember);
        application.setReviewedAt(LocalDateTime.now());

        AdmissionApplication updatedApplication = applicationRepository.save(application);
        return convertToDto(updatedApplication);
    }

    @Transactional
    public UserProfileDto acceptApplicationAndRegisterStudent(UUID applicationId, String staffEmail) {
        // 1. Fetch all necessary entities
        AdmissionApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        SchoolClass schoolClass = application.getApplyingClass();
        if (schoolClass == null) {
            throw new IllegalStateException("Application is not linked to a class.");
        }

        // 2. Validation Checks
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Application must be in APPROVED state to register the student.");
        }
        if (userRepository.findByEmail(application.getApplicantEmail()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists.");
        }

        // 3. --- AUTOMATION LOGIC ---

        // a. Check Section Capacity
        long currentEnrollment = userRepository.countBySchoolClass(schoolClass);
        if (schoolClass.getSectionCapacity() > 0 && currentEnrollment >= schoolClass.getSectionCapacity()) {
            throw new IllegalStateException("The selected class section is full. Please create a new section.");
        }

        // b. Generate the next Roll Number
        String branchCode = schoolClass.getGradeLevel().replaceAll("[^a-zA-Z]", "").toUpperCase();
        long nextSequence = currentEnrollment + 1;
        String rollNumber = String.format("%s-%s-%03d", branchCode, schoolClass.getSection(), nextSequence);


        // 4. Prepare the registration request for the UserService
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFullName(application.getApplicantName());
        registrationRequest.setEmail(application.getApplicantEmail());
        registrationRequest.setContactNumber(application.getContactNumber());
        registrationRequest.setRole(Role.ROLE_STUDENT);
        registrationRequest.setClassIds(Set.of(schoolClass.getClassId()));

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        registrationRequest.setPassword(tempPassword);

        // 5. Use the existing UserService to create the user in the database
        User newStudentUser = userService.registerUser(registrationRequest, staffEmail);

        // 6. Set the new automated fields on the user
        newStudentUser.setRollNumber(rollNumber);
        // Note: Mentor assignment logic will be added in the next step (UI update)

        userRepository.save(newStudentUser); // Save the updated user with roll number

        // 7. Update the application to link to the new user and mark as PAID
        application.setStudentUser(newStudentUser);
        application.setStatus(ApplicationStatus.PAID);
        applicationRepository.save(application);

        // 8. Create a DTO to return to the staff member
        UserProfileDto profile = new UserProfileDto();
        profile.setUserId(newStudentUser.getUserId());
        profile.setFullName(newStudentUser.getFullName());
        profile.setEmail(newStudentUser.getEmail());
        profile.setRole(newStudentUser.getRole());
        profile.setRollNumber(newStudentUser.getRollNumber());
        profile.setPassword(tempPassword);

        return profile;
    }

    @Transactional(readOnly = true)
    public AdmissionApplicationDto getApplicationStatusByEmail(String email) {
        AdmissionApplication application = applicationRepository.findByApplicantEmail(email)
                .orElseThrow(() -> new RuntimeException("No application found with the provided email address."));
        return convertToDto(application);
    }

    @Transactional
    public UserProfileDto finalizeApplicationAndRegisterStudent(UUID applicationId) {
        AdmissionApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new IllegalStateException("Application must be in APPROVED state to finalize.");
        }
        if (userRepository.findByEmail(application.getApplicantEmail()).isPresent()) {
            throw new IllegalStateException("A user with this email already exists.");
        }

        UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
        registrationRequest.setFullName(application.getApplicantName());
        registrationRequest.setEmail(application.getApplicantEmail());
        registrationRequest.setContactNumber(application.getContactNumber());
        registrationRequest.setRole(Role.ROLE_STUDENT);

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        registrationRequest.setPassword(tempPassword);

        if (application.getApplyingClass() != null) {
            registrationRequest.setClassIds(Set.of(application.getApplyingClass().getClassId()));
        } else {
            throw new IllegalStateException("Application is not linked to a class.");
        }

        User newStudentUser = userService.registerUser(registrationRequest);

        application.setStudentUser(newStudentUser);
        application.setStatus(ApplicationStatus.PAID);
        applicationRepository.save(application);

        UserProfileDto profile = new UserProfileDto();
        profile.setUserId(newStudentUser.getUserId());
        profile.setFullName(newStudentUser.getFullName());
        profile.setEmail(newStudentUser.getEmail());
        profile.setRole(newStudentUser.getRole());
        profile.setPassword(tempPassword);

        return profile;
    }

    private AdmissionApplicationDto convertToDto(AdmissionApplication app) {
        String reviewedByName = (app.getReviewedBy() != null) ? app.getReviewedBy().getFullName() : null;

        UserProfileDto studentUserDto = null;
        if (app.getStudentUser() != null) {
            User student = app.getStudentUser();
            studentUserDto = new UserProfileDto();
            studentUserDto.setUserId(student.getUserId());
            studentUserDto.setFullName(student.getFullName());
            studentUserDto.setEmail(student.getEmail());
            studentUserDto.setContactNumber(student.getContactNumber());
            studentUserDto.setRole(student.getRole());
        }

        String programName = (app.getApplyingClass() != null) ? (app.getApplyingClass().getGradeLevel() + " - " + app.getApplyingClass().getSection()) : null;

        return new AdmissionApplicationDto(
                app.getApplicationId(), app.getApplicantName(), app.getApplicantEmail(),
                app.getContactNumber(), app.getPreviousEducationDetails(), programName,
                app.getStatus(), app.getAppliedAt(), reviewedByName, studentUserDto
        );
    }
}