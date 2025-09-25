package com.sih.erp.controller;

import com.sih.erp.dto.CreateTimetableSlotRequest;
import com.sih.erp.dto.TimetableSlotDto;
import com.sih.erp.service.TimetableService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TimetableController {

    @Autowired
    private TimetableService timetableService;

    // Endpoint for Staff to create a new timetable slot
    @PostMapping("/staff/timetable")
//    @PreAuthorize("hasAuthority('ROLE_ADMISSIONS_STAFF')")
    public ResponseEntity<TimetableSlotDto> createTimetableSlot(@Valid @RequestBody CreateTimetableSlotRequest request, Principal principal) {
        TimetableSlotDto createdSlot = timetableService.createTimetableSlot(request, principal.getName());
        return new ResponseEntity<>(createdSlot, HttpStatus.CREATED);
    }

    // Public endpoint for anyone authenticated to view a class's timetable
    @GetMapping("/timetable/class/{classId}")
//    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TimetableSlotDto>> getTimetableForClass(@PathVariable UUID classId) {
        List<TimetableSlotDto> timetable = timetableService.getTimetableForClass(classId);
        return ResponseEntity.ok(timetable);
    }

    // ... inside TimetableController

    // Endpoint for a logged-in teacher to get their personal timetable
    @GetMapping("/teacher/timetable/me")
//    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<List<TimetableSlotDto>> getTeacherTimetable(Principal principal) {
        List<TimetableSlotDto> timetable = timetableService.getTimetableForTeacher(principal.getName());
        return ResponseEntity.ok(timetable);
    }

    // Endpoint for a logged-in student to get their class timetable
    @GetMapping("/student/timetable/me")
//    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<List<TimetableSlotDto>> getStudentTimetable(Principal principal) {
        List<TimetableSlotDto> timetable = timetableService.getTimetableForStudent(principal.getName());
        return ResponseEntity.ok(timetable);
    }
}