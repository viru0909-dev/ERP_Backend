package com.sih.erp.service;

import com.sih.erp.dto.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.sih.erp.repository.QuizRepository;
import com.sih.erp.dto.QuizListDto;
import com.sih.erp.dto.CourseDetailsDto;
import com.sih.erp.dto.QuizDto;
import com.sih.erp.repository.QuizRepository;
import java.security.Principal;


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
    @Autowired private QuizRepository quizRepository;



    // --- Module Methods ---

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

        if (file != null && !file.isEmpty()) {
            String fileUrl = fileStorageService.storeFile(file);
            newModule.setFileUrl(fileUrl);
        }

        return moduleRepository.save(newModule);
    }

    @Transactional(readOnly = true)
    public List<CourseModuleDto> getModulesForCourse(UUID classId, UUID subjectId) {
        List<CourseModule> modules;
        if (subjectId == null) {
            // Find all modules for a class, regardless of subject
            modules = moduleRepository.findBySchoolClass_ClassId(classId);
        } else {
            // Find modules for a specific subject within a class
            modules = moduleRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId);
        }

        return modules.stream()
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
    public void deleteModule(UUID moduleId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found"));
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module not found"));

        if (!module.getCreatedBy().equals(teacher)) {
            throw new SecurityException("You are not authorized to delete this module.");
        }
        moduleRepository.delete(module);
    }

    // --- Assignment Methods ---

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
        List<Assignment> assignments;
        if (subjectId == null) {
            // Find all assignments for a class, regardless of subject
            assignments = assignmentRepository.findBySchoolClass_ClassId(classId);
        } else {
            // Find assignments for a specific subject within a class
            assignments = assignmentRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId);
        }

        return assignments.stream()
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

    // --- Student-Facing Method (BUG FIX) ---

    // Replace your existing findCoursesByStudent method with this one.
    @Transactional(readOnly = true)
    public List<StudentCourseDto> findCoursesByStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with email: " + studentEmail));

        if (student.getRole() != Role.ROLE_STUDENT || student.getSchoolClass() == null) {
            return List.of();
        }

        SchoolClass schoolClass = student.getSchoolClass();

        // Fetch ONLY the subjects for the student's class
        Set<SubjectDto> subjects = courseRepository.findBySchoolClass_ClassId(schoolClass.getClassId())
                .stream()
                .map(course -> new SubjectDto(course.getSubject().getSubjectId(), course.getSubject().getName()))
                .collect(Collectors.toSet());

        // Create the DTO with only the data needed for this page
        StudentCourseDto courseDto = new StudentCourseDto();
        courseDto.setClassId(schoolClass.getClassId());
        courseDto.setGradeLevel(schoolClass.getGradeLevel());
        courseDto.setSection(schoolClass.getSection());
        courseDto.setSubjects(subjects);
        // Note: We no longer set modules, assignments, or quizzes here

        return List.of(courseDto);
    }
    // --- Other Management Methods ---

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
        subjectRepository.deleteById(subjectId);
    }

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
        if (userRepository.countBySchoolClass_ClassId(classId) > 0) {
            throw new IllegalStateException("Cannot delete a class that has students enrolled.");
        }
        schoolClassRepository.deleteById(classId);
    }

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
        if (timetableSlotRepository.existsByClassroom_ClassroomId(classroomId)) {
            throw new IllegalStateException("Cannot delete a classroom that is currently in use in the timetable.");
        }
        classroomRepository.deleteById(classroomId);
    }

    @Transactional
    public void designClass(UUID classId, List<CourseDesignDto> courseDesigns) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

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
    @Transactional(readOnly = true)
    public CourseDetailsDto getCourseDetails(UUID classId, UUID subjectId, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found: " + principal.getName()));


        // Fetch Modules
        List<CourseModuleDto> modules = moduleRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId)
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

        // Fetch Assignments
        List<AssignmentDto> assignments = assignmentRepository.findBySchoolClass_ClassIdAndSubject_SubjectId(classId, subjectId)
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

        // Fetch Quizzes created by this specific teacher for this subject
        List<QuizDto> quizzes;
        if (user.getRole() == Role.ROLE_TEACHER) {
            // If the user is a teacher, find quizzes they created for this subject
            quizzes = quizRepository.findBySubject_SubjectIdAndCreatedBy(subjectId, user)
                    .stream()
                    .map(QuizDto::new)
                    .collect(Collectors.toList());
        } else {
            // If the user is a student, find all quizzes for this subject in their class
            Subject subject = subjectRepository.findById(subjectId).orElseThrow();
            quizzes = quizRepository.findBySubjectIn(List.of(subject))
                    .stream()
                    .map(QuizDto::new)
                    .collect(Collectors.toList());
        }

        return new CourseDetailsDto(modules, assignments, quizzes);
    }


}