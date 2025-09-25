package com.sih.erp.controller;

import com.sih.erp.dto.TeacherProfileDto;
import com.sih.erp.dto.UserRegistrationRequest;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.entity.Role;
import com.sih.erp.entity.User;
import com.sih.erp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class UserManagementController {

    @Autowired
    private UserService userService;

    @PostMapping("/staff/register")
//    @PreAuthorize("hasAnyAuthority('ROLE_ACADEMIC_ADMIN', 'ROLE_SUPER_STAFF')")
    public ResponseEntity<?> registerCampusUser(@Valid @RequestBody UserRegistrationRequest request, Principal principal) {
        if (!EnumSet.of(Role.ROLE_STUDENT, Role.ROLE_TEACHER).contains(request.getRole())) {
            return ResponseEntity.badRequest().body("This user can only register Students or Teachers.");
        }
        User registeredUser = userService.registerUser(request, principal.getName());
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setUserId(registeredUser.getUserId());
        userProfileDto.setFullName(registeredUser.getFullName());
        userProfileDto.setEmail(registeredUser.getEmail());
        userProfileDto.setContactNumber(registeredUser.getContactNumber());
        userProfileDto.setRole(registeredUser.getRole());
        return new ResponseEntity<>(userProfileDto, HttpStatus.CREATED);
    }

    @PostMapping("/admin/register")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_STAFF')")
    public ResponseEntity<?> registerStaff(@Valid @RequestBody UserRegistrationRequest request, Principal principal) {
        // Updated to check for the new, specific staff roles
        if (!EnumSet.of(Role.ROLE_ADMISSIONS_STAFF, Role.ROLE_ACADEMIC_ADMIN, Role.ROLE_HOSTEL_ADMIN).contains(request.getRole())) {
            return ResponseEntity.badRequest().body("Super Staff can only register other staff members.");
        }
        User registeredUser = userService.registerUser(request, principal.getName());
        UserProfileDto userProfileDto = new UserProfileDto();
        userProfileDto.setUserId(registeredUser.getUserId());
        userProfileDto.setFullName(registeredUser.getFullName());
        userProfileDto.setEmail(registeredUser.getEmail());
        userProfileDto.setContactNumber(registeredUser.getContactNumber());
        userProfileDto.setRole(registeredUser.getRole());
        return new ResponseEntity<>(userProfileDto, HttpStatus.CREATED);
    }

    @GetMapping("/staff/registered-teachers")
//    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<List<TeacherProfileDto>> getRegisteredTeachers(Principal principal) {
        return ResponseEntity.ok(userService.findTeacherProfilesRegisteredBy(principal.getName()));
    }

    @GetMapping("/staff/registered-students")
//    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<List<UserProfileDto>> getRegisteredStudents(Principal principal) {
        return ResponseEntity.ok(userService.findRegisteredUsersByRole(principal.getName(), Role.ROLE_STUDENT));
    }

    @GetMapping("/staff/teachers/by-class/{classId}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ACADEMIC_ADMIN', 'ROLE_SUPER_STAFF')")
    public ResponseEntity<List<UserProfileDto>> getTeachersByClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(userService.findTeachersByClass(classId));
    }

    @DeleteMapping("/staff/user/{userId}")
//    @PreAuthorize("hasAnyAuthority('ROLE_ACADEMIC_ADMIN', 'ROLE_SUPER_STAFF')")
    public ResponseEntity<?> deleteRegisteredUser(@PathVariable UUID userId, Principal principal) {
        try {
            userService.deleteUserAsStaff(userId, principal.getName());
            return ResponseEntity.ok().body("User removed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/admin/staff")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_STAFF')")
    public ResponseEntity<List<UserProfileDto>> getAllStaff() {
        // Updated to use the new service method that finds all staff types
        return ResponseEntity.ok(userService.findAllStaffMembers());
    }

    @DeleteMapping("/admin/staff/{userId}")
//    @PreAuthorize("hasAuthority('ROLE_SUPER_STAFF')")
    public ResponseEntity<?> deleteStaffMember(@PathVariable UUID userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().body("User deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/teacher/class/{classId}/students")
//    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<List<UserProfileDto>> getStudentsInClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(userService.findStudentsByClass(classId));
    }
}