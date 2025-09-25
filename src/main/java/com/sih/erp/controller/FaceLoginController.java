// Create new file: src/main/java/com/sih/erp/controller/FaceLoginController.java

package com.sih.erp.controller;

import com.sih.erp.dto.JwtAuthenticationResponse;
import com.sih.erp.entity.User;
import com.sih.erp.service.FaceAuthService;
import com.sih.erp.service.UserDetailsServiceImpl;
import com.sih.erp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FaceLoginController {

    @Autowired private FaceAuthService faceAuthService;
    @Autowired private UserDetailsServiceImpl userDetailsService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/api/public/auth/face/login")
    public ResponseEntity<?> loginWithFace(@RequestParam("email") String email, @RequestParam("file") MultipartFile file) {
        try {
            User authenticatedUser = faceAuthService.authenticateUserByFace(email, file);
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticatedUser.getEmail());
            final String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new JwtAuthenticationResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}