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
import org.springframework.web.multipart.MultipartFile; // <-- Add this import
import org.springframework.web.bind.annotation.RequestParam; // <-- Add this import


import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
//@PreAuthorize("hasAuthority('ROLE_TEACHER')") // Secures all endpoints in this controller
public class CourseController {

    @Autowired
    private CourseService courseService;

    // --- Module Endpoints ---



    @GetMapping("/{classId}/{subjectId}/modules")
    public ResponseEntity<List<CourseModuleDto>> getModules(@PathVariable UUID classId, @PathVariable UUID subjectId) {
        return ResponseEntity.ok(courseService.getModulesForCourse(classId, subjectId));
    }

    // --- Assignment Endpoints ---

    @PostMapping("/{classId}/{subjectId}/assignments")
    public ResponseEntity<?> createAssignment(@PathVariable UUID classId, @PathVariable UUID subjectId, @Valid @RequestBody CreateAssignmentRequest request, Principal principal) {
        try {
            // The service now returns the created assignment entity
            Assignment newAssignment = courseService.createAssignment(classId, subjectId, request, principal.getName());

            // We convert it to a DTO before sending it back
            AssignmentDto newAssignmentDto = new AssignmentDto(
                    newAssignment.getAssignmentId(),
                    newAssignment.getTitle(),
                    newAssignment.getInstructions(),
                    newAssignment.getDueDate(),
                    newAssignment.getAssignedAt(),
                    newAssignment.getCreatedBy().getFullName()
            );

            // Return the full object with a 201 CREATED status
            return new ResponseEntity<>(newAssignmentDto, HttpStatus.CREATED);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{classId}/{subjectId}/assignments")
    public ResponseEntity<List<AssignmentDto>> getAssignments(@PathVariable UUID classId, @PathVariable UUID subjectId) {
        return ResponseEntity.ok(courseService.getAssignmentsForCourse(classId, subjectId));
    }

    @PostMapping("/{classId}/{subjectId}/modules")
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

    @DeleteMapping("/modules/{moduleId}")
    public ResponseEntity<?> deleteModule(@PathVariable UUID moduleId, Principal principal) {
        try {
            courseService.deleteModule(moduleId, principal.getName());
            return ResponseEntity.ok().body("Module deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }


}