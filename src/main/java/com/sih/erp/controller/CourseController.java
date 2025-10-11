package com.sih.erp.controller;

import com.sih.erp.dto.*;
import com.sih.erp.entity.Assignment;
import com.sih.erp.entity.CourseModule;
import com.sih.erp.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping("/{classId}/{subjectId}/details")
    // --- THIS IS THE FIX ---
    // Changed from hasAuthority("ROLE_TEACHER") to allow both roles
    @PreAuthorize("hasAnyAuthority('ROLE_TEACHER', 'ROLE_STUDENT')")
    public ResponseEntity<CourseDetailsDto> getCourseDetails(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            Principal principal) {
        // The service logic will determine what data to return based on the user's role
        CourseDetailsDto courseDetails = courseService.getCourseDetails(classId, subjectId, principal);
        return ResponseEntity.ok(courseDetails);
    }

    // --- CREATE AND DELETE ENDPOINTS (Remain the same) ---

    @PostMapping("/{classId}/{subjectId}/modules")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> createModule(
            @PathVariable UUID classId,
            @PathVariable UUID subjectId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {
        try {
            CourseModule newModule = courseService.createModule(classId, subjectId, title, description, file, principal.getName());
            CourseModuleDto newModuleDto = new CourseModuleDto(
                    newModule.getModuleId(), newModule.getTitle(), newModule.getDescription(),
                    newModule.getFileUrl(), newModule.getCreatedAt(), newModule.getCreatedBy().getFullName()
            );
            return new ResponseEntity<>(newModuleDto, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{classId}/{subjectId}/assignments")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> createAssignment(@PathVariable UUID classId, @PathVariable UUID subjectId, @Valid @RequestBody CreateAssignmentRequest request, Principal principal) {
        try {
            Assignment newAssignment = courseService.createAssignment(classId, subjectId, request, principal.getName());
            AssignmentDto newAssignmentDto = new AssignmentDto(
                    newAssignment.getAssignmentId(), newAssignment.getTitle(), newAssignment.getInstructions(),
                    newAssignment.getDueDate(), newAssignment.getAssignedAt(), newAssignment.getCreatedBy().getFullName()
            );
            return new ResponseEntity<>(newAssignmentDto, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/modules/{moduleId}")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> deleteModule(@PathVariable UUID moduleId, Principal principal) {
        try {
            courseService.deleteModule(moduleId, principal.getName());
            return ResponseEntity.ok().body("Module deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}