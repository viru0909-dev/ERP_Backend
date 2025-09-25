package com.sih.erp.controller;

import com.sih.erp.service.FaceAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/face")
@CrossOrigin(origins = "http://localhost:5173")
public class FaceAuthController {

    @Autowired
    private FaceAuthService faceAuthService;

    // This is the bouncer that will fix your error.
    @PostMapping("/register")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMISSIONS_STAFF', 'ROLE_ACADEMIC_ADMIN', 'ROLE_HOSTEL_ADMIN', 'ROLE_SUPER_STAFF')")
    public ResponseEntity<?> registerFace(@RequestParam("userId") UUID userId, @RequestParam("file") MultipartFile file) {
        try {
            boolean success = faceAuthService.registerUserFace(userId, file);
            if (success) {
                return ResponseEntity.ok("Face registered successfully.");
            }
            return ResponseEntity.status(500).body("Failed to register face.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}