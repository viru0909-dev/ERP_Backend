// src/main/java/com/sih/erp/repository/CourseRepository.java
package com.sih.erp.repository;

import com.sih.erp.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findBySchoolClass_ClassId(UUID classId);

}