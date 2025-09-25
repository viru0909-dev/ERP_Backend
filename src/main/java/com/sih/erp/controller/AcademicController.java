// In src/main/java/com/sih/erp/controller/AcademicController.java

package com.sih.erp.controller;

import com.sih.erp.dto.*; // Ensure all your DTOs are imported
import com.sih.erp.service.CourseService;
import com.sih.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Make sure this is imported
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/academic")
//@PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')") // <-- THIS IS THE FIX
public class AcademicController {

    @Autowired private UserService userService;
    @Autowired private CourseService courseService;

    // Endpoint for getting students in a class
    @GetMapping("/class/{classId}/students")
    public ResponseEntity<List<UserProfileDto>> getStudentsInClass(@PathVariable UUID classId) {
        return ResponseEntity.ok(userService.findStudentsByClass(classId));
    }

    // Endpoint for updating student status
    @PutMapping("/students/{studentId}/status")
    public ResponseEntity<UserProfileDto> updateStudentStatus(
            @PathVariable UUID studentId,
            @RequestBody StudentStatusUpdateRequest request) {

        UserProfileDto updatedStudent = userService.updateStudentStatus(
                studentId,
                request.isFeePaid(),
                request.getAcademicStatus()
        );
        return ResponseEntity.ok(updatedStudent);
    }

    // Endpoint for promoting students
    @PostMapping("/students/promote")
    public ResponseEntity<?> promoteStudents(@RequestBody StudentPromotionRequest request) {
        userService.promoteStudents(request.getStudentIds(), request.getNextClassId());
        return ResponseEntity.ok("Students promoted successfully.");
    }

    // Endpoint for designing a class curriculum
    @PostMapping("/class/{classId}/design")
    public ResponseEntity<?> designClass(@PathVariable UUID classId, @RequestBody List<CourseDesignDto> request) {
        courseService.designClass(classId, request);
        return ResponseEntity.ok("Class curriculum designed successfully.");
    }

    // Endpoint for assigning mentors
    @PostMapping("/students/assign-mentor")
    public ResponseEntity<?> assignMentor(@RequestBody MentorAssignmentRequest request) {
        userService.assignMentorToStudents(request.getStudentIds(), request.getMentorId());
        return ResponseEntity.ok("Mentor assigned successfully.");
    }
    @GetMapping("/class/{classId}/design")
    public ResponseEntity<List<CourseDto>> getCourseDesign(@PathVariable UUID classId) {
        return ResponseEntity.ok(courseService.getCourseDesignForClass(classId));
    }
}