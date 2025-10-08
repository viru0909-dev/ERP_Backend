package com.sih.erp.service;

import com.sih.erp.dto.CreateQuizRequestDto;
import com.sih.erp.entity.Question;
import com.sih.erp.entity.Quiz;
import com.sih.erp.entity.Subject;
import com.sih.erp.entity.User;
import com.sih.erp.repository.QuizRepository;
import com.sih.erp.repository.SubjectRepository;
import com.sih.erp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sih.erp.dto.SubmitQuizRequestDto;
import com.sih.erp.entity.QuizAttempt;
import com.sih.erp.entity.XPTransaction;
import com.sih.erp.repository.QuizAttemptRepository;
import com.sih.erp.repository.XPTransactionRepository;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GamificationService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private XPTransactionRepository xpTransactionRepository;


    @Transactional // Ensures the entire operation succeeds or fails together
    public Quiz createQuiz(CreateQuizRequestDto request, Principal principal) {
        // 1. Find the teacher (User) who is creating the quiz
        User teacher = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // 2. Find the subject this quiz belongs to
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        // 3. Create the main Quiz entity
        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(request.getTitle());
        newQuiz.setSubject(subject);
        newQuiz.setCreatedBy(teacher);

        // 4. Create the Question entities from the DTO and link them to the new quiz
        List<Question> questions = new ArrayList<>();
        for (CreateQuizRequestDto.QuestionDto questionDto : request.getQuestions()) {
            Question question = new Question();
            question.setQuestionText(questionDto.getQuestionText());
            question.setOptions(questionDto.getOptions());
            question.setCorrectOptionIndex(questionDto.getCorrectOptionIndex());
            question.setQuiz(newQuiz); // Link the question back to its parent quiz
            questions.add(question);
        }
        newQuiz.setQuestions(questions);

        // 5. Save the Quiz. Thanks to CascadeType.ALL, the questions will be saved automatically.
        return quizRepository.save(newQuiz);
    }

    @Transactional
    public QuizAttempt submitQuiz(UUID quizId, SubmitQuizRequestDto submission, Principal principal) {
        // 1. Find the student and the quiz
        User student = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        // 2. Check if the student has already taken this quiz
        if (quizAttemptRepository.existsByStudent_UserIdAndQuiz_QuizId(student.getUserId(), quizId)) {
            throw new IllegalStateException("You have already completed this quiz.");
        }

        // 3. Get the correct answers for this quiz for easy lookup
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
        attempt.setScore(score);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        quizAttemptRepository.save(attempt);

        // 6. Award XP based on the score (e.g., 10 XP per correct answer)
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
}