package com.sih.erp.repository;

import com.sih.erp.entity.Role;
import com.sih.erp.entity.SchoolClass;
import com.sih.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // JpaRepository<EntityType, PrimaryKeyType>

    // Spring Data JPA will automatically provide methods like:
    // - save()
    // - findById()
    // - findAll()
    // - delete()
    // ...and many more!

    // We can also define custom query methods here later if needed.
    // For example, to find a user by their email:
     Optional<User> findByEmail(String email);
    List<User> findByFaceEmbeddingIsNotNull();
    List<User> findByRole(Role role);

    List<User> findByRegisteredBy_EmailAndRole(String email, Role role);



    List<User> findByRegisteredByAndRole(User registeredBy, Role role);
    List<User> findByTaughtClassesContains(SchoolClass schoolClass);

    List<User> findBySchoolClass(SchoolClass schoolClass);
    List<User> findByRoleIn(List<Role> roles);

    List<User> findByRegisteredBy(User registrar);

    long countBySchoolClass(SchoolClass schoolClass);

    long countBySchoolClass_ClassId(UUID classId);

    long countByRole(Role role);
    @Query("SELECT s.schoolClass.gradeLevel, COUNT(s) FROM User s WHERE s.role = 'ROLE_STUDENT' GROUP BY s.schoolClass.gradeLevel")
    List<Object[]> countStudentsPerClass();

    List<User> findByMentor_UserId(UUID mentorId);
}
