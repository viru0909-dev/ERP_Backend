package com.sih.erp.repository;

import com.sih.erp.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    // You can add custom query methods here later
    // For example: List<Quiz> findBySubjectId(UUID subjectId);
}