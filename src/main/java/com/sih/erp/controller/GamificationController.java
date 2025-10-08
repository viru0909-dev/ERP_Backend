package com.sih.erp.controller;

import com.sih.erp.dto.CreateQuizRequestDto;
import com.sih.erp.dto.QuizDto;
import com.sih.erp.entity.Quiz;
import com.sih.erp.service.GamificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sih.erp.dto.QuizAttemptResultDto;
import com.sih.erp.dto.SubmitQuizRequestDto;
import com.sih.erp.entity.QuizAttempt;
import org.springframework.web.bind.annotation.PathVariable;
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
}