package com.sih.erp.controller;

import com.sih.erp.dto.AttendanceRecordDto;
import com.sih.erp.dto.AttendanceSubmissionDto;
import com.sih.erp.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    // Endpoint for a TEACHER to submit attendance for their class
    @PostMapping("/teacher/attendance")
//    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> submitAttendance(@RequestBody AttendanceSubmissionDto submission, Principal principal) {
        try {
            attendanceService.markAttendance(submission, principal.getName());
            return ResponseEntity.ok("Attendance submitted successfully.");
        } catch (Exception e) {
            // Return a more specific error message to the frontend
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint for a STUDENT to view their own attendance records
    @GetMapping("/student/attendance/me")
//    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<List<AttendanceRecordDto>> getMyAttendance(Principal principal) {
        List<AttendanceRecordDto> records = attendanceService.getAttendanceForStudent(principal.getName());
        return ResponseEntity.ok(records);
    }
}