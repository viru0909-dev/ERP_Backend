// Create new file: src/main/java/com/sih/erp/controller/TeacherController.java
package com.sih.erp.controller;

import com.sih.erp.dto.ExaminationDtos;
import com.sih.erp.dto.RiskProfileDto;
import com.sih.erp.service.ExaminationService;
import com.sih.erp.service.RiskAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasAuthority('ROLE_TEACHER')")
public class TeacherController {

    @Autowired private ExaminationService examinationService;
    @Autowired private RiskAnalysisService riskAnalysisService;

    // Endpoint for uploading marks
    @PostMapping("/marks/upload")
    public ResponseEntity<?> uploadMarks(@RequestBody ExaminationDtos.MarksUploadRequestDto request, Principal principal) {
        examinationService.uploadMarks(request, principal.getName());
        return ResponseEntity.ok("Marks uploaded successfully.");
    }

    // Endpoint for the Risk Dashboard
    @GetMapping("/risk-dashboard")
    public ResponseEntity<List<RiskProfileDto>> getRiskDashboard(Principal principal) {
        List<RiskProfileDto> profiles = riskAnalysisService.getRiskProfilesForMentor(principal.getName());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/risk-profile/{studentId}")
    public ResponseEntity<RiskProfileDto> getRiskProfileDetails(@PathVariable UUID studentId) {
        RiskProfileDto profile = riskAnalysisService.getDetailedRiskProfile(studentId);
        return ResponseEntity.ok(profile);
    }
}