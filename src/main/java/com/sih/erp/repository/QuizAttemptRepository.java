package com.sih.erp.repository;

import com.sih.erp.entity.Quiz;
import com.sih.erp.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    // This method is crucial to check if a student has already taken a quiz
    boolean existsByStudent_UserIdAndQuiz_QuizId(UUID userId, UUID quizId);
    // Add this new method
    void deleteByQuiz(Quiz quiz);

    // Add these two new methods to your QuizAttemptRepository interface
    int countByStudent_UserIdAndQuiz_QuizId(UUID userId, UUID quizId);
    List<QuizAttempt> findByQuiz_QuizIdOrderByCompletedAtDesc(UUID quizId);
}