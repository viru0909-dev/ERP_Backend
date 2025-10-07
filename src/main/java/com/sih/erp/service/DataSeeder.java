package com.sih.erp.service;

import com.sih.erp.entity.Role;
import com.sih.erp.entity.SchoolClass;
import com.sih.erp.entity.Subject;
import com.sih.erp.entity.User; // <-- ADD THIS IMPORT
import com.sih.erp.repository.SchoolClassRepository;
import com.sih.erp.repository.SubjectRepository;
import com.sih.erp.repository.UserRepository; // <-- ADD THIS IMPORT
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder; // <-- ADD THIS IMPORT
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID; // <-- ADD THIS IMPORT

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SchoolClassRepository schoolClassRepository;

    // --- NEW DEPENDENCIES TO ADD ---
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    // -----------------------------

    @Override
    public void run(String... args) throws Exception {
        logger.info("DataSeeder running...");
        seedSubjects();
        seedSchoolClasses();
        seedSuperStaffUser(); // <-- ADD CALL TO THE NEW METHOD
        logger.info("DataSeeder finished.");
    }

    // --- NEW METHOD TO ADD ---
    private void seedSuperStaffUser() {
        if (userRepository.findByRole(Role.ROLE_SUPER_STAFF).isEmpty()) {
            logger.info("No SUPER_STAFF found. Creating initial admin user...");

            User admin = new User();
            admin.setUserId(UUID.randomUUID());
            admin.setFullName("Virendra Admin");
            admin.setEmail("virendra.admin@sih.com");
            admin.setPassword(passwordEncoder.encode("superSecretPassword123")); // Securely hashed
            admin.setContactNumber("1234567890");
            admin.setRole(Role.ROLE_SUPER_STAFF);

            userRepository.save(admin);
            logger.info("Initial SUPER_STAFF user created successfully.");
        } else {
            logger.info("SUPER_STAFF user already exists. Skipping seed.");
        }
    }
    // -------------------------

    private void seedSubjects() {
        if (subjectRepository.count() == 0) {
            logger.info("No subjects found in DB. Seeding...");
            List<Subject> subjects = Arrays.asList(
                    createSubject("Core Java"),
                    createSubject("Advanced Java"),
                    createSubject("Database Management Systems"),
                    createSubject("Python Programming"),
                    createSubject("Data Structures & Algorithms"),
                    createSubject("Operating Systems"),
                    createSubject("Computer Networks"),
                    createSubject("Web Development"),
                    createSubject("Software Engineering"),
                    createSubject("Cloud Computing"),
                    createSubject("Machine Learning"),
                    createSubject("Cybersecurity"),
                    createSubject("Discrete Mathematics"),
                    createSubject("Object-Oriented Programming"),
                    createSubject("Artificial Intelligence")
            );
            subjectRepository.saveAll(subjects);
            logger.info("Seeded {} subjects.", subjects.size());
        } else {
            logger.info("Subjects already exist in DB. Skipping seed.");
        }
    }

    private void seedSchoolClasses() {
        if (schoolClassRepository.count() == 0) {
            logger.info("No school classes found in DB. Seeding...");
            List<SchoolClass> classes = Arrays.asList(
                    createSchoolClass("BCA", "FY"),
                    createSchoolClass("BCA", "SY"),
                    createSchoolClass("BCA", "TY"),
                    createSchoolClass("MCA", "FY"),
                    createSchoolClass("MCA", "SY"),
                    createSchoolClass("B.Sc. CS", "FY"),
                    createSchoolClass("B.Sc. CS", "SY"),
                    createSchoolClass("B.Sc. CS", "TY"),
                    createSchoolClass("11th", "Science"),
                    createSchoolClass("12th", "Science")
            );
            schoolClassRepository.saveAll(classes);
            logger.info("Seeded {} school classes.", classes.size());
        } else {
            logger.info("School classes already exist in DB. Skipping seed.");
        }
    }

    // Helper methods to reduce boilerplate
    private Subject createSubject(String name) {
        Subject subject = new Subject();
        subject.setName(name);
        return subject;
    }

    private SchoolClass createSchoolClass(String grade, String section) {
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setGradeLevel(grade);
        schoolClass.setSection(section);
        return schoolClass;
    }
}