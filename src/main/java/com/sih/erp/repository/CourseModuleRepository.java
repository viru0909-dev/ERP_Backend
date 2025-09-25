package com.sih.erp.repository;

import com.sih.erp.entity.CourseModule;
import com.sih.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {
    // Find all modules for a specific subject in a specific class
    List<CourseModule> findBySchoolClass_ClassIdAndSubject_SubjectId(UUID classId, UUID subjectId);

    List<CourseModule> findByCreatedBy(User user);
}
