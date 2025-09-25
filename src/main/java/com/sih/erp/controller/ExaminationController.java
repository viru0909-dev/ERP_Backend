// Create new file: src/main/java/com/sih/erp/controller/ExaminationController.java
package com.sih.erp.controller;

import com.sih.erp.dto.ExaminationDtos.*;
import com.sih.erp.entity.ExamType;
import com.sih.erp.service.ExaminationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ExaminationController {

    @Autowired
    private ExaminationService examinationService;

//    @PostMapping("/teacher/marks/upload")
//    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
//    public ResponseEntity<?> uploadMarks(@RequestBody MarksUploadRequestDto request, Principal principal) {
//        examinationService.uploadMarks(request, principal.getName());
//        return ResponseEntity.ok("Marks uploaded successfully.");
//    }

    @GetMapping("/student/result")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<ResultCardDto> getResult(
            Principal principal,
            @RequestParam("examType") ExamType examType) { // <-- ADD THIS PARAMETER

        return ResponseEntity.ok(examinationService.getStudentResultCard(principal.getName(), examType));
    }
}