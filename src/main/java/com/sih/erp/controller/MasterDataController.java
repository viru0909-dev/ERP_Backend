package com.sih.erp.controller;

import com.sih.erp.dto.SchoolClassDetailsDto;
import com.sih.erp.dto.SchoolClassDto;
import com.sih.erp.dto.UserProfileDto;
import com.sih.erp.entity.Role;
import com.sih.erp.entity.SchoolClass;
import com.sih.erp.entity.Subject;
import com.sih.erp.repository.SchoolClassRepository;
import com.sih.erp.repository.SubjectRepository;
import com.sih.erp.dto.ClassroomDto;

import com.sih.erp.repository.UserRepository;
import com.sih.erp.service.CourseService;
import com.sih.erp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sih.erp.entity.Classroom; // <-- Add this import
import com.sih.erp.repository.ClassroomRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/master")
//@PreAuthorize("hasAnyAuthority('ROLE_STAFF', 'ROLE_SUPER_STAFF')")
public class MasterDataController {

    @Autowired
    private CourseService courseService;



    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectRepository.findAll();
        return ResponseEntity.ok(subjects);
    }
    @GetMapping("/classes")
    public ResponseEntity<List<SchoolClass>> getAllClasses() {
        return ResponseEntity.ok(schoolClassRepository.findAll());
    }

    @GetMapping("/classrooms")
    public ResponseEntity<List<Classroom>> getAllClassrooms() {
        return ResponseEntity.ok(classroomRepository.findAll());
    }

    @GetMapping("/classes/{classId}")
    public ResponseEntity<SchoolClassDetailsDto> getClassDetails(@PathVariable UUID classId) {
        return ResponseEntity.ok(courseService.findClassDetailsById(classId));
    }

    @GetMapping("/teachers")
    //@PreAuthorize("hasAuthority('ROLE_STAFF')") // Only staff need to access this
    public ResponseEntity<List<UserProfileDto>> getAllTeachers() {
        return ResponseEntity.ok(userService.findAllByRole(Role.ROLE_TEACHER));
    }

    // ... inside MasterDataController.java

// --- ADD THESE NEW SECURE ENDPOINTS ---

    @PostMapping("/subjects")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<Subject> createSubject(@RequestBody String subjectName) {
        // Assuming the request body is just the plain string name
        return new ResponseEntity<>(courseService.createSubject(subjectName), HttpStatus.CREATED);
    }

    @PutMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<Subject> updateSubject(@PathVariable UUID subjectId, @RequestBody String newSubjectName) {
        return ResponseEntity.ok(courseService.updateSubject(subjectId, newSubjectName));
    }

    @DeleteMapping("/subjects/{subjectId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<?> deleteSubject(@PathVariable UUID subjectId) {
        courseService.deleteSubject(subjectId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/classes")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<SchoolClass> createSchoolClass(@RequestBody SchoolClassDto classDto) {
        return new ResponseEntity<>(courseService.createSchoolClass(classDto), HttpStatus.CREATED);
    }

    @PutMapping("/classes/{classId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<SchoolClass> updateSchoolClass(@PathVariable UUID classId, @RequestBody SchoolClassDto classDto) {
        return ResponseEntity.ok(courseService.updateSchoolClass(classId, classDto));
    }

    @DeleteMapping("/classes/{classId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<?> deleteSchoolClass(@PathVariable UUID classId) {
        try {
            courseService.deleteSchoolClass(classId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/classrooms")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<Classroom> createClassroom(@RequestBody ClassroomDto classroomDto) {
        return new ResponseEntity<>(courseService.createClassroom(classroomDto), HttpStatus.CREATED);
    }

    @PutMapping("/classrooms/{classroomId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<Classroom> updateClassroom(@PathVariable UUID classroomId, @RequestBody ClassroomDto classroomDto) {
        return ResponseEntity.ok(courseService.updateClassroom(classroomId, classroomDto));
    }

    @DeleteMapping("/classrooms/{classroomId}")
    @PreAuthorize("hasAuthority('ROLE_ACADEMIC_ADMIN')")
    public ResponseEntity<?> deleteClassroom(@PathVariable UUID classroomId) {
        try {
            courseService.deleteClassroom(classroomId);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}