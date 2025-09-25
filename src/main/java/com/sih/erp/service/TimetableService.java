package com.sih.erp.service;

import com.sih.erp.dto.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TimetableService {

    @Autowired private TimetableSlotRepository timetableSlotRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SchoolClassRepository schoolClassRepository;
    @Autowired private SubjectRepository subjectRepository;

    @Transactional
    public TimetableSlotDto createTimetableSlot(CreateTimetableSlotRequest request, String staffEmail) {
        User staffMember = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Staff user not found: " + staffEmail));

        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new RuntimeException("School Class not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        TimetableSlot newSlot = new TimetableSlot();
        newSlot.setDayOfWeek(request.getDayOfWeek());
        newSlot.setStartTime(request.getStartTime());
        newSlot.setEndTime(request.getEndTime());
        newSlot.setSchoolClass(schoolClass);
        newSlot.setSubject(subject);
        newSlot.setTeacher(teacher);
        newSlot.setCreatedBy(staffMember);

        TimetableSlot savedSlot = timetableSlotRepository.save(newSlot);
        return convertToDto(savedSlot);
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getTimetableForClass(UUID classId) {
        return timetableSlotRepository.findBySchoolClass_ClassIdOrderByDayOfWeekAscStartTimeAsc(classId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getTimetableForTeacher(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found: " + teacherEmail));

        return timetableSlotRepository.findByTeacher_UserIdOrderByDayOfWeekAscStartTimeAsc(teacher.getUserId())
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotDto> getTimetableForStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found: " + studentEmail));

        if (student.getSchoolClass() == null) {
            return List.of();
        }

        UUID classId = student.getSchoolClass().getClassId();
        return getTimetableForClass(classId);
    }



    private TimetableSlotDto convertToDto(TimetableSlot slot) {
        // --- THIS IS THE FIX ---
        // Use setters to create the DTOs safely

        SchoolClassDto classDto = new SchoolClassDto();
        classDto.setClassId(slot.getSchoolClass().getClassId());
        classDto.setGradeLevel(slot.getSchoolClass().getGradeLevel());
        classDto.setSection(slot.getSchoolClass().getSection());
        classDto.setSectionCapacity(slot.getSchoolClass().getSectionCapacity());

        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setSubjectId(slot.getSubject().getSubjectId());
        subjectDto.setName(slot.getSubject().getName());

        UserProfileDto teacherDto = new UserProfileDto();
        teacherDto.setUserId(slot.getTeacher().getUserId());
        teacherDto.setFullName(slot.getTeacher().getFullName());
        teacherDto.setEmail(slot.getTeacher().getEmail());
        teacherDto.setContactNumber(slot.getTeacher().getContactNumber());
        teacherDto.setRole(slot.getTeacher().getRole());

        // The TimetableSlotDto constructor is fine to use as it's less likely to change.
        return new TimetableSlotDto(slot.getSlotId(), slot.getDayOfWeek(), slot.getStartTime(), slot.getEndTime(), classDto, subjectDto, teacherDto);
    }

}