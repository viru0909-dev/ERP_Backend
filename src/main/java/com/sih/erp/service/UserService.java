package com.sih.erp.service;

import com.sih.erp.dto.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final SchoolClassRepository schoolClassRepository;
    @Autowired private TimetableSlotRepository timetableSlotRepository;
    @Autowired private CourseModuleRepository courseModuleRepository;
    @Autowired private AssignmentRepository assignmentRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private XPTransactionRepository xpTransactionRepository;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SubjectRepository subjectRepository, SchoolClassRepository schoolClassRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.schoolClassRepository = schoolClassRepository;
    }

    @Transactional
    public User registerUser(UserRegistrationRequest registrationRequest) {
        User newUser = new User();
        newUser.setFullName(registrationRequest.getFullName());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setContactNumber(registrationRequest.getContactNumber());
        newUser.setRole(registrationRequest.getRole());
        return userRepository.save(newUser);
    }

    @Transactional
    public User registerUser(UserRegistrationRequest registrationRequest, String registrarEmail) {
        User registrar = userRepository.findByEmail(registrarEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Registrar user not found with email: " + registrarEmail));

        User newUser = new User();
        newUser.setFullName(registrationRequest.getFullName());
        newUser.setEmail(registrationRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        newUser.setContactNumber(registrationRequest.getContactNumber());
        newUser.setRole(registrationRequest.getRole());
        newUser.setRegisteredBy(registrar);

        if (registrationRequest.getRole() == Role.ROLE_STUDENT) {
            if (registrationRequest.getClassIds() != null && !registrationRequest.getClassIds().isEmpty()) {
                UUID studentClassId = registrationRequest.getClassIds().iterator().next();
                SchoolClass studentClass = schoolClassRepository.findById(studentClassId)
                        .orElseThrow(() -> new RuntimeException("Class not found for student registration"));
                newUser.setSchoolClass(studentClass);
            }
        }

        if (registrationRequest.getRole() == Role.ROLE_TEACHER) {
            if (registrationRequest.getSubjectIds() != null && !registrationRequest.getSubjectIds().isEmpty()) {
                Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(registrationRequest.getSubjectIds()));
                newUser.setTaughtSubjects(subjects);
            }
            if (registrationRequest.getClassIds() != null && !registrationRequest.getClassIds().isEmpty()) {
                Set<SchoolClass> classes = new HashSet<>(schoolClassRepository.findAllById(registrationRequest.getClassIds()));
                newUser.setTaughtClasses(classes);
            }
        }

        return userRepository.save(newUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        List<User> usersRegisteredBy = userRepository.findByRegisteredBy(userToDelete);
        for (User user : usersRegisteredBy) {
            user.setRegisteredBy(null);
        }
        userRepository.saveAll(usersRegisteredBy);

        timetableSlotRepository.deleteAll(timetableSlotRepository.findByCreatedBy(userToDelete));
        timetableSlotRepository.deleteAll(timetableSlotRepository.findByTeacher(userToDelete));
        courseModuleRepository.deleteAll(courseModuleRepository.findByCreatedBy(userToDelete));
        assignmentRepository.deleteAll(assignmentRepository.findByCreatedBy(userToDelete));

        userRepository.delete(userToDelete);
    }

    @Transactional
    public void deleteUserAsStaff(UUID userIdToDelete, String registrarEmail) {
        User registrar = userRepository.findByEmail(registrarEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Registrar not found"));

        User userToDelete = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new UsernameNotFoundException("User to delete not found"));

        if (userToDelete.getRegisteredBy() == null || !userToDelete.getRegisteredBy().equals(registrar)) {
            throw new SecurityException("You are not authorized to remove this user.");
        }

        deleteUser(userIdToDelete);
    }

    // --- DTO CONVERSION AND FINDER METHODS ---

    // In src/main/java/com/sih/erp/service/UserService.java

    // In src/main/java/com/sih/erp/service/UserService.java

    private UserProfileDto convertToUserProfileDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getUserId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setContactNumber(user.getContactNumber());
        dto.setRole(user.getRole());
        dto.setRollNumber(user.getRollNumber());
        dto.setFeePaid(user.isFeePaid());
        dto.setAcademicStatus(user.getAcademicStatus());

        // ADD THIS LOGIC
        if (user.getMentor() != null) {
            dto.setMentorName(user.getMentor().getFullName());
        }

        if (user.getRole() == Role.ROLE_STUDENT) {
            Integer totalXp = xpTransactionRepository.findTotalXpByUserId(user.getUserId());
            dto.setTotalXp(totalXp != null ? totalXp : 0);
        }

        return dto;
    }

    public UserProfileDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return convertToUserProfileDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> findRegisteredUsersByRole(String registrarEmail, Role role) {
        User registrar = userRepository.findByEmail(registrarEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Registrar not found with email: " + registrarEmail));

        return userRepository.findByRegisteredByAndRole(registrar, role)
                .stream()
                .map(this::convertToUserProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> findAllByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToUserProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> findTeachersByClass(UUID classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));

        return userRepository.findByTaughtClassesContains(schoolClass).stream()
                .filter(user -> user.getRole() == Role.ROLE_TEACHER)
                .map(this::convertToUserProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> findStudentsByClass(UUID classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found with id: " + classId));

        return userRepository.findBySchoolClass(schoolClass).stream()
                .map(this::convertToUserProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherProfileDto findTeacherProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Teacher not found with email: " + email));

        TeacherProfileDto teacherProfile = new TeacherProfileDto();
        teacherProfile.setUserId(user.getUserId());
        teacherProfile.setFullName(user.getFullName());
        teacherProfile.setEmail(user.getEmail());
        teacherProfile.setContactNumber(user.getContactNumber());
        teacherProfile.setRole(user.getRole());

        // --- THIS IS THE FIX ---
        // Use the safer "setter" pattern instead of constructors

        Set<SubjectDto> subjectDtos = user.getTaughtSubjects().stream()
                .map(subject -> {
                    SubjectDto dto = new SubjectDto();
                    dto.setSubjectId(subject.getSubjectId());
                    dto.setName(subject.getName());
                    return dto;
                })
                .collect(Collectors.toSet());
        teacherProfile.setTaughtSubjects(subjectDtos);

        Set<SchoolClassDto> classDtos = user.getTaughtClasses().stream()
                .map(cls -> {
                    SchoolClassDto dto = new SchoolClassDto();
                    dto.setClassId(cls.getClassId());
                    dto.setGradeLevel(cls.getGradeLevel());
                    dto.setSection(cls.getSection());
                    dto.setSectionCapacity(cls.getSectionCapacity());
                    return dto;
                })
                .collect(Collectors.toSet());
        teacherProfile.setTaughtClasses(classDtos);

        return teacherProfile;
    }
    @Transactional(readOnly = true)
    public List<TeacherProfileDto> findTeacherProfilesRegisteredBy(String registrarEmail) {
        User registrar = userRepository.findByEmail(registrarEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Registrar not found with email: " + registrarEmail));

        return userRepository.findByRegisteredByAndRole(registrar, Role.ROLE_TEACHER)
                .stream()
                .map(teacher -> findTeacherProfileByEmail(teacher.getEmail()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileDto> findAllStaffMembers() {
        List<Role> staffRoles = List.of(Role.ROLE_ADMISSIONS_STAFF, Role.ROLE_ACADEMIC_ADMIN, Role.ROLE_HOSTEL_ADMIN);
        return userRepository.findByRoleIn(staffRoles)
                .stream()
                .map(this::convertToUserProfileDto) // Using the helper method we created
                .collect(Collectors.toList());
    }

    public UserProfileDto findUserProfileById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return convertToUserProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateStudentStatus(UUID studentId, boolean feePaid, AcademicStatus status) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found with id: " + studentId));

        if (student.getRole() != Role.ROLE_STUDENT) {
            throw new IllegalArgumentException("This operation is only for students.");
        }

        student.setFeePaid(feePaid);
        student.setAcademicStatus(status);

        User updatedStudent = userRepository.save(student);
        return convertToUserProfileDto(updatedStudent); // Assuming you have this helper method
    }

    @Transactional
    public void promoteStudents(List<UUID> studentIds, UUID nextClassId) {
        SchoolClass nextClass = schoolClassRepository.findById(nextClassId)
                .orElseThrow(() -> new RuntimeException("Target class for promotion not found"));

        List<User> studentsToPromote = userRepository.findAllById(studentIds);

        for (User student : studentsToPromote) {
            // Add a check to ensure we only promote students who have passed
            if (student.getRole() == Role.ROLE_STUDENT && student.getAcademicStatus() == AcademicStatus.PASS) {
                student.setSchoolClass(nextClass);
                // Optional: Reset their status for the new academic year
                student.setAcademicStatus(AcademicStatus.PENDING);
                student.setFeePaid(false);
            }
        }
        userRepository.saveAll(studentsToPromote);
    }

    // In src/main/java/com/sih/erp/service/UserService.java

    @Transactional
    public void assignMentorToStudents(List<UUID> studentIds, UUID mentorId) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new UsernameNotFoundException("Mentor not found with id: " + mentorId));

        if (mentor.getRole() != Role.ROLE_TEACHER) {
            throw new IllegalArgumentException("Mentors must be teachers.");
        }

        List<User> students = userRepository.findAllById(studentIds);

        for (User student : students) {
            if (student.getRole() == Role.ROLE_STUDENT) {
                student.setMentor(mentor);
            }
        }

        userRepository.saveAll(students);
    }

    @Transactional
    public void createAnnouncement(AnnouncementRequestDto request, String staffEmail) {
        User staff = userRepository.findByEmail(staffEmail).orElseThrow(/* ... */);
        Set<SchoolClass> targetClasses = new HashSet<>(schoolClassRepository.findAllById(request.getTargetClassIds()));

        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setCreatedBy(staff);
        announcement.setTargetClasses(targetClasses);

        announcementRepository.save(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> getAnnouncementsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(/* ... */);
        SchoolClass userClass = null;

        if(user.getRole() == Role.ROLE_STUDENT) {
            userClass = user.getSchoolClass();
        } else if (user.getRole() == Role.ROLE_TEACHER && !user.getTaughtClasses().isEmpty()) {
            // For simplicity, we'll get announcements for the first class a teacher is assigned to.
            // A more complex system could merge announcements from all their classes.
            userClass = user.getTaughtClasses().iterator().next();
        }

        if (userClass == null) {
            return List.of();
        }

        return announcementRepository.findByTargetClassesContainsOrderByCreatedAtDesc(userClass)
                .stream().map(this::convertToAnnouncementDto).collect(Collectors.toList());
    }
    // In src/main/java/com/sih/erp/service/UserService.java

    private AnnouncementDto convertToAnnouncementDto(Announcement announcement) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setCreatedAt(announcement.getCreatedAt());

        // Safely get the creator's name
        if (announcement.getCreatedBy() != null) {
            dto.setCreatedBy(announcement.getCreatedBy().getFullName());
        }

        return dto;
    }

    @Transactional
    public void changePassword(String userEmail, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(userEmail).orElseThrow(/*...*/);

        // 1. Verify the old password is correct
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password.");
        }

        // 2. Encode and set the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}