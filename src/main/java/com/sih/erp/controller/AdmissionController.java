package com.sih.erp.controller;

import com.sih.erp.dto.AdmissionApplicationDto;
import com.sih.erp.dto.AdmissionRequestDto;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.service.AdmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AdmissionController {

    @Autowired
    private AdmissionService admissionService;

    // A PUBLIC endpoint for anyone to submit an application
    @PostMapping("/public/admissions/apply")
    public ResponseEntity<?> submitApplication(@Valid @RequestBody AdmissionRequestDto request) {
        admissionService.submitApplication(request);
        return ResponseEntity.ok("Application submitted successfully. You will be notified of the outcome via email.");
    }

    // A STAFF-ONLY endpoint to view pending applications
    @GetMapping("/staff/admissions/pending")
//    @PreAuthorize("hasAuthority('ROLE_ADMISSIONS_STAFF')")
    public ResponseEntity<List<AdmissionApplicationDto>> getPendingApplications() {
        return ResponseEntity.ok(admissionService.getPendingApplications());
    }

    // A STAFF-ONLY endpoint to approve an application
    @PostMapping("/staff/admissions/{applicationId}/approve")
//    @PreAuthorize("hasAuthority('ROLE_ADMISSIONS_STAFF')")
    public ResponseEntity<AdmissionApplicationDto> approveApplication(@PathVariable UUID applicationId, Principal principal) {
        AdmissionApplicationDto approvedApplication = admissionService.approveApplication(applicationId, principal.getName());
        return ResponseEntity.ok(approvedApplication);
    }

    @PostMapping("/staff/admissions/{applicationId}/register-student")
//    @PreAuthorize("hasAuthority('ROLE_ADMISSIONS_STAFF')")
    public ResponseEntity<?> registerStudentFromApplication(@PathVariable UUID applicationId, Principal principal) {
        try {
            UserProfileDto newStudentProfile = admissionService.acceptApplicationAndRegisterStudent(applicationId, principal.getName());
            return ResponseEntity.ok(newStudentProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/public/admissions/status")
    public ResponseEntity<?> getApplicationStatus(@RequestParam String email) {
        try {
            AdmissionApplicationDto applicationStatus = admissionService.getApplicationStatusByEmail(email);
            return ResponseEntity.ok(applicationStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/public/admissions/{applicationId}/finalize")
    public ResponseEntity<?> finalizeApplication(@PathVariable UUID applicationId) {
        try {
            UserProfileDto newStudentProfile = admissionService.finalizeApplicationAndRegisterStudent(applicationId);
            return ResponseEntity.ok(newStudentProfile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}