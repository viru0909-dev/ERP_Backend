package com.sih.erp.repository;

import com.sih.erp.entity.Quiz;
import com.sih.erp.entity.Subject; // <-- Add this import

import com.sih.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    // You can add custom query methods here later
    // For example: List<Quiz> findBySubjectId(UUID subjectId);
    List<Quiz> findBySubjectIn(List<Subject> subjects);
    // Add this new method to your QuizRepository interface
    List<Quiz> findByCreatedBy(User user);
    // Add this new method
    List<Quiz> findBySubject_SubjectIdAndCreatedBy(UUID subjectId, User teacher);

    // Add this new method
}