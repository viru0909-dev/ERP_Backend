package com.sih.erp.controller;

import com.sih.erp.dto.*;
import com.sih.erp.entity.Quiz;
import com.sih.erp.service.GamificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sih.erp.entity.QuizAttempt;

import java.util.List;
import java.util.UUID;

import java.security.Principal;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    @Autowired
    private GamificationService gamificationService;

    /**
     * Endpoint for a teacher to create a new quiz with its questions.
     * Accessible only by users with the 'ROLE_TEACHER'.
     */
    @PostMapping("/quizzes")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> createQuiz(@Valid @RequestBody CreateQuizRequestDto request, Principal principal) {
        try {
            // 1. Call the service to create the quiz
            Quiz createdQuiz = gamificationService.createQuiz(request, principal);

            // 2. Convert the created entity to a DTO for the response
            QuizDto responseDto = new QuizDto(createdQuiz);

            // 3. Return the new quiz data with a 201 CREATED status
            return new ResponseEntity<>(responseDto, HttpStatus.CREATED);

        } catch (Exception e) {
            // Handle any errors (e.g., subject not found) and return a bad request status
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/quizzes/{quizId}/submit")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<?> submitQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody SubmitQuizRequestDto submission,
            Principal principal) {
        try {
            // 1. Call the service to process the submission, calculate score, and award XP
            QuizAttempt result = gamificationService.submitQuiz(quizId, submission, principal);

            // 2. Convert the result to a DTO for the response
            QuizAttemptResultDto resultDto = new QuizAttemptResultDto(result);

            // 3. Return the result to the student
            return ResponseEntity.ok(resultDto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Add this new method
    @GetMapping("/quizzes/{quizId}/take")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<QuizForStudentDto> getQuizForStudent(@PathVariable UUID quizId) {
        QuizForStudentDto quizDto = gamificationService.getQuizForStudent(quizId);
        return ResponseEntity.ok(quizDto);
    }

    // Add these new methods to your GamificationController class

    @GetMapping("/quizzes/teacher")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<List<QuizDto>> getTeacherQuizzes(Principal principal) {
        List<QuizDto> quizzes = gamificationService.getQuizzesByTeacher(principal);
        return ResponseEntity.ok(quizzes);
    }

    @DeleteMapping("/quizzes/{quizId}")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<?> deleteQuiz(@PathVariable UUID quizId, Principal principal) {
        try {
            gamificationService.deleteQuiz(quizId, principal);
            return ResponseEntity.ok().body("Quiz deleted successfully.");
        } catch (Exception e) {
            // This will catch both "Quiz not found" and the SecurityException
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    // Add this new method
    @GetMapping("/quizzes/{quizId}/results")
    @PreAuthorize("hasAuthority('ROLE_TEACHER')")
    public ResponseEntity<List<QuizAttemptDto>> getQuizResults(@PathVariable UUID quizId) {
        List<QuizAttemptDto> results = gamificationService.getResultsForQuiz(quizId);
        return ResponseEntity.ok(results);
    }
}