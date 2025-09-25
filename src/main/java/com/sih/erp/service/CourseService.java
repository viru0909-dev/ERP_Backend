package com.sih.erp.service;

import com.sih.erp.dto.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.sih.erp.entity.Course; // <-- ADD THIS LINE

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private SchoolClassRepository schoolClassRepository;
    @Autowired private CourseModuleRepository moduleRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private FileStorageService fileStorageService;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private TimetableSlotRepository timetableSlotRepository;
    @Autowired private CourseRepository courseRepository;

    @Transactional
    public CourseModule createModule(UUID classId, UUID subjectId, String title, String description, MultipartFile file, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail).orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new RuntimeException("Subject not found"));

        CourseModule newModule = new CourseModule();
        newModule.setTitle(title);
        newModule.setDescription(description);
        newModule.setSchoolClass(schoolClass);
        newModule.setSubject(subject);
        newModule.setCreatedBy(teacher);

        // If a file is present, store it and set the URL
        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.storeFile(file);
            newModule.setFileUrl(fileUrl);
        }

        return moduleRepository.save(newModule);
    }
    @Transactional(readOnly = true)
    public List<CourseModuleDto> getModulesForCourse(UUID classId, UUID subjectId) {
        return moduleRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId)
                .stream()
                .map(module -> new CourseModuleDto(
                        module.getModuleId(),
                        module.getTitle(),
                        module.getDescription(),
                        module.getFileUrl(),
                        module.getCreatedAt(),
                        module.getCreatedBy().getFullName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Assignment createAssignment(UUID classId, UUID subjectId, CreateAssignmentRequest request, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail).orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
        SchoolClass schoolClass = schoolClassRepository.findById(classId).orElseThrow(() -> new RuntimeException("Class not found"));
        Subject subject = subjectRepository.findById(subjectId).orElseThrow(() -> new RuntimeException("Subject not found"));

        Assignment newAssignment = new Assignment();
        newAssignment.setTitle(request.getTitle());
        newAssignment.setInstructions(request.getInstructions());
        newAssignment.setDueDate(request.getDueDate());
        newAssignment.setSchoolClass(schoolClass);
        newAssignment.setSubject(subject);
        newAssignment.setCreatedBy(teacher);

        return assignmentRepository.save(newAssignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> getAssignmentsForCourse(UUID classId, UUID subjectId) {
        return assignmentRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId)
                .stream()
                .map(assignment -> new AssignmentDto(
                        assignment.getAssignmentId(),
                        assignment.getTitle(),
                        assignment.getInstructions(),
                        assignment.getDueDate(),
                        assignment.getAssignedAt(),
                        assignment.getCreatedBy().getFullName()
                ))
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteModule(UUID moduleId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        // SECURITY CHECK: Ensure the teacher deleting the module is the one who created it.
        if (!module.getCreatedBy().equals(teacher)) {
            throw new SecurityException("You are not authorized to delete this module.");
        }

        moduleRepository.delete(module);
    }

    // ... inside the CourseService class

    // In src/main/java/com/sih/erp/service/CourseService.java

    @Transactional(readOnly = true)
    public List<StudentCourseDto> findCoursesByStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with email: " + studentEmail));

        if (student.getRole() != Role.ROLE_STUDENT || student.getSchoolClass() == null) {
            return List.of(); // Return empty if not a student or not in a class
        }

        SchoolClass schoolClass = student.getSchoolClass();

        // NEW LOGIC: Directly query the Course table for this class's curriculum
        Set<SubjectDto> subjects = courseRepository.findBySchoolClass_ClassId(schoolClass.getClassId())
                .stream()
                .map(course -> new SubjectDto(course.getSubject().getSubjectId(), course.getSubject().getName()))
                .collect(Collectors.toSet());

        StudentCourseDto courseDto = new StudentCourseDto(
                schoolClass.getClassId(),
                schoolClass.getGradeLevel(),
                schoolClass.getSection(),
                subjects
        );

        return List.of(courseDto);
    }
    @Transactional(readOnly = true)
    public SchoolClassDetailsDto findClassDetailsById(UUID classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("School Class not found"));

        List<User> teachersForClass = userRepository.findByTaughtClassesContains(schoolClass);

        Set<SubjectDto> subjects = teachersForClass.stream()
                .flatMap(teacher -> teacher.getTaughtSubjects().stream())
                .map(subject -> new SubjectDto(subject.getSubjectId(), subject.getName()))
                .collect(Collectors.toSet());

        return new SchoolClassDetailsDto(
                schoolClass.getClassId(), schoolClass.getGradeLevel(), schoolClass.getSection(),
                schoolClass.getDurationInYears(), schoolClass.getFeeStructure(),
                schoolClass.getHighestPackage(), schoolClass.getProgramHighlights(), subjects
        );
    }

    // ... inside CourseService.java

// --- ADD THESE NEW METHODS FOR SUBJECT MANAGEMENT ---

    @Transactional
    public Subject createSubject(String subjectName) {
        Subject newSubject = new Subject();
        newSubject.setName(subjectName);
        return subjectRepository.save(newSubject);
    }

    @Transactional
    public Subject updateSubject(UUID subjectId, String newSubjectName) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));
        subject.setName(newSubjectName);
        return subjectRepository.save(subject);
    }

    @Transactional
    public void deleteSubject(UUID subjectId) {
        // We should add checks here to prevent deleting a subject that's in use
        subjectRepository.deleteById(subjectId);
    }

    // ... inside CourseService.java

// --- ADD THESE NEW METHODS FOR SCHOOL CLASS MANAGEMENT ---

    @Transactional
    public SchoolClass createSchoolClass(SchoolClassDto classDto) {
        SchoolClass newClass = new SchoolClass();
        newClass.setGradeLevel(classDto.getGradeLevel());
        newClass.setSection(classDto.getSection());
        newClass.setSectionCapacity(classDto.getSectionCapacity());
        return schoolClassRepository.save(newClass);
    }

    @Transactional
    public SchoolClass updateSchoolClass(UUID classId, SchoolClassDto classDto) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("SchoolClass not found"));
        schoolClass.setGradeLevel(classDto.getGradeLevel());
        schoolClass.setSection(classDto.getSection());
        schoolClass.setSectionCapacity(classDto.getSectionCapacity());
        return schoolClassRepository.save(schoolClass);
    }

    @Transactional
    public void deleteSchoolClass(UUID classId) {
        // Basic check to prevent deleting a class that has students.
        // A more robust check would also look at timetable slots, teacher assignments, etc.
        if (userRepository.countBySchoolClass_ClassId(classId) > 0) {
            throw new IllegalStateException("Cannot delete a class that has students enrolled.");
        }
        schoolClassRepository.deleteById(classId);
    }

    // ... inside CourseService.java

// --- ADD THESE NEW METHODS FOR CLASSROOM MANAGEMENT ---

    @Transactional
    public Classroom createClassroom(ClassroomDto classroomDto) {
        Classroom newClassroom = new Classroom();
        newClassroom.setRoomNumber(classroomDto.getRoomNumber());
        newClassroom.setCapacity(classroomDto.getCapacity());
        return classroomRepository.save(newClassroom);
    }

    @Transactional
    public Classroom updateClassroom(UUID classroomId, ClassroomDto classroomDto) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));
        classroom.setRoomNumber(classroomDto.getRoomNumber());
        classroom.setCapacity(classroomDto.getCapacity());
        return classroomRepository.save(classroom);
    }

    @Transactional
    public void deleteClassroom(UUID classroomId) {
        // Safety check: Prevent deleting a classroom that is scheduled in the timetable
        if (timetableSlotRepository.existsByClassroom_ClassroomId(classroomId)) {
            throw new IllegalStateException("Cannot delete a classroom that is currently in use in the timetable.");
        }
        classroomRepository.deleteById(classroomId);
    }

    // In CourseService.java
    @Transactional
    public void designClass(UUID classId, List<CourseDesignDto> courseDesigns) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        // Optional: Clear existing design for this class before applying new one
        // courseRepository.deleteBySchoolClass(schoolClass);

        List<Course> courses = new ArrayList<>();
        for (CourseDesignDto design : courseDesigns) {
            Subject subject = subjectRepository.findById(design.getSubjectId()).orElseThrow(/*...*/);
            User teacher = userRepository.findById(design.getTeacherId()).orElseThrow(/*...*/);

            Course course = new Course();
            course.setSchoolClass(schoolClass);
            course.setSubject(subject);
            course.setTeacher(teacher);
            courses.add(course);
        }
        courseRepository.saveAll(courses);
    }
    @Transactional(readOnly = true)
    public List<CourseDto> getCourseDesignForClass(UUID classId) {
        return courseRepository.findBySchoolClass_ClassId(classId).stream()
                .map(course -> new CourseDto(
                        course.getCourseId(),
                        course.getSubject().getSubjectId(),
                        course.getSubject().getName(),
                        course.getTeacher().getUserId(),
                        course.getTeacher().getFullName()
                ))
                .collect(Collectors.toList());
    }

}

