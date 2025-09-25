package com.sih.erp.service;

import com.sih.erp.dto.AttendanceRecordDto;
import com.sih.erp.dto.AttendanceSubmissionDto;
import com.sih.erp.entity.*;
import com.sih.erp.repository.AttendanceRecordRepository;
import com.sih.erp.repository.TimetableSlotRepository;
import com.sih.erp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired private AttendanceRecordRepository attendanceRepository;
    @Autowired private TimetableSlotRepository timetableSlotRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public void markAttendance(AttendanceSubmissionDto submission, String teacherEmail) {
        TimetableSlot slot = timetableSlotRepository.findById(submission.getTimetableSlotId())
                .orElseThrow(() -> new RuntimeException("Timetable slot not found"));

        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // --- SMART VALIDATION ---

        // 1. Authorization Check: Is this teacher assigned to this timetable slot?
        if (!slot.getTeacher().equals(teacher)) {
            throw new AccessDeniedException("You are not authorized to take attendance for this class.");
        }

//        // 2. Time Check: Is the class currently active? (Allow marking up to 15 mins after class ends)
//        LocalTime now = LocalTime.now();
//        if (now.isBefore(slot.getStartTime()) || now.isAfter(slot.getEndTime().plusMinutes(15))) {
//            throw new IllegalStateException("Attendance can only be taken during or shortly after the scheduled class time.");
//        }

        // 3. Duplicate Check: Has attendance already been taken for this class today?
        if (attendanceRepository.existsByDateAndTimetableSlot_SlotId(LocalDate.now(), slot.getSlotId())) {
            throw new IllegalStateException("Attendance has already been submitted for this class today.");
        }

        // --- SAVE RECORDS ---
        List<AttendanceRecord> recordsToSave = new ArrayList<>();
        for (Map.Entry<UUID, AttendanceStatus> entry : submission.getStudentStatuses().entrySet()) {
            User student = userRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Student not found with ID: " + entry.getKey()));

            AttendanceRecord record = new AttendanceRecord();
            record.setDate(LocalDate.now());
            record.setStatus(entry.getValue());
            record.setStudent(student);
            record.setTimetableSlot(slot);
            record.setMarkedBy(teacher);
            recordsToSave.add(record);
        }

        attendanceRepository.saveAll(recordsToSave);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAttendanceForStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return attendanceRepository.findByStudent_UserIdOrderByDateDesc(student.getUserId())
                .stream()
                .map(record -> new AttendanceRecordDto(
                        record.getAttendanceId(),
                        record.getDate(),
                        record.getStatus(),
                        record.getTimetableSlot().getSubject().getName()
                ))
                .collect(Collectors.toList());
    }
}