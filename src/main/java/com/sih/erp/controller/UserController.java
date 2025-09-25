package com.sih.erp.controller;

import com.sih.erp.dto.ChangePasswordRequest;
import com.sih.erp.dto.TeacherProfileDto;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.entity.User;
import com.sih.erp.repository.UserRepository;
import com.sih.erp.service.FaceAuthService;
import com.sih.erp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;
     private FaceAuthService faceAuthService; // <-- ADD THIS


    public UserController(UserService userService, FaceAuthService faceAuthService) {
        this.userService = userService;
        this.faceAuthService = faceAuthService; // <-- INITIALIZE IT HERE
    }
    /**
     * A protected endpoint to get the profile of the currently authenticated user.
     * This is for non-teacher roles.
     * @param principal Automatically injected by Spring Security.
     * @return UserProfileDto containing non-sensitive user data.
     */

    @PostMapping("/me/face/register")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> registerMyFace(@RequestParam("file") MultipartFile file, Principal principal) {
        try {
            faceAuthService.registerUserFace(principal.getName(), file);
            return ResponseEntity.ok("Face registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMISSIONS_STAFF', 'ROLE_ACADEMIC_ADMIN', 'ROLE_HOSTEL_ADMIN', 'ROLE_SUPER_STAFF', 'ROLE_STUDENT')")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(Principal principal) {
        String email = principal.getName();
        UserProfileDto userProfile = userService.findUserByEmail(email);
        return ResponseEntity.ok(userProfile);
    }

    /**
     * A protected endpoint specifically for teachers to get their detailed profile.
     * @param principal Automatically injected by Spring Security.
     * @return TeacherProfileDto containing teacher-specific data like subjects and classes.
     */
    @GetMapping("/me/teacher")
//    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<TeacherProfileDto> getTeacherProfile(Principal principal) {
        String email = principal.getName();
        TeacherProfileDto teacherProfile = userService.findTeacherProfileByEmail(email);
        return ResponseEntity.ok(teacherProfile);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileDto> getUserProfileById(@PathVariable UUID userId) {
        UserProfileDto userProfile = userService.findUserProfileById(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request, Principal principal) {
        try {
            userService.changePassword(principal.getName(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
