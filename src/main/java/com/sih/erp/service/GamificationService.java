package com.sih.erp.service;

import com.sih.erp.dto.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GamificationService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private XPTransactionRepository xpTransactionRepository;

    private static final int MAX_QUIZ_ATTEMPTS = 3;

    @Transactional
    public Quiz createQuiz(CreateQuizRequestDto request, Principal principal) {
        User teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(request.getTitle());
        newQuiz.setSubject(subject);
        newQuiz.setCreatedBy(teacher);

        List<Question> questions = new ArrayList<>();
        for (CreateQuizRequestDto.QuestionDto questionDto : request.getQuestions()) {
            Question question = new Question();
            question.setQuestionText(questionDto.getQuestionText());
            question.setOptions(questionDto.getOptions());
            question.setCorrectOptionIndex(questionDto.getCorrectOptionIndex());
            question.setQuiz(newQuiz);
            questions.add(question);
        }
        newQuiz.setQuestions(questions);

        return quizRepository.save(newQuiz);
    }

    @Transactional
    public QuizAttempt submitQuiz(UUID quizId, SubmitQuizRequestDto submission, Principal principal) {
        // 1. Find the student and the quiz
        User student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // 2. Check attempt count before proceeding
        int currentAttempts = quizAttemptRepository.countByStudent_UserIdAndQuiz_QuizId(student.getUserId(), quizId);
        if (currentAttempts >= MAX_QUIZ_ATTEMPTS) {
            throw new IllegalStateException("You have reached the maximum number of attempts for this quiz.");
        }

        // 3. Get correct answers for scoring
        Map<UUID, Integer> correctAnswers = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getQuestionId, Question::getCorrectOptionIndex));

        // 4. Calculate the score
        int score = 0;
        for (Map.Entry<UUID, Integer> submittedAnswer : submission.getAnswers().entrySet()) {
            UUID questionId = submittedAnswer.getKey();
            Integer studentAnswerIndex = submittedAnswer.getValue();
            if (studentAnswerIndex.equals(correctAnswers.get(questionId))) {
                score++;
            }
        }

        // 5. Create and save the quiz attempt record
        QuizAttempt attempt = new QuizAttempt();
        attempt.setStudent(student);
        attempt.setQuiz(quiz);
        attempt.setScore(score); // Use the calculated score
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setAttemptNumber(currentAttempts + 1); // Set the new attempt number
        quizAttemptRepository.save(attempt);

        // 6. Award XP based on the score
        int pointsEarned = score * 10;
        if (pointsEarned > 0) {
            XPTransaction xpTransaction = new XPTransaction();
            xpTransaction.setUser(student);
            xpTransaction.setPoints(pointsEarned);
            xpTransaction.setReason("Completed quiz: " + quiz.getTitle());
            xpTransactionRepository.save(xpTransaction);
        }

        return attempt;
    }

    public List<QuizDto> getQuizzesByTeacher(Principal principal) {
        User teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        return quizRepository.findByCreatedBy(teacher)
                .stream()
                .map(QuizDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteQuiz(UUID quizId, Principal principal) {
        User teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (!quiz.getCreatedBy().getUserId().equals(teacher.getUserId())) {
            throw new SecurityException("You are not authorized to delete this quiz.");
        }

        // First, delete all student attempts for this quiz.
        quizAttemptRepository.deleteByQuiz(quiz);
        // Now, it's safe to delete the quiz itself.
        quizRepository.delete(quiz);
    }

    public List<QuizAttemptDto> getResultsForQuiz(UUID quizId) {
        return quizAttemptRepository.findByQuiz_QuizIdOrderByCompletedAtDesc(quizId)
                .stream()
                .map(QuizAttemptDto::new)
                .collect(Collectors.toList());
    }

    public QuizForStudentDto getQuizForStudent(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        return new QuizForStudentDto(quiz);
    }
}