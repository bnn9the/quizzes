package edu.platform.service;

import edu.platform.dto.request.QuizSubmissionRequest;
import edu.platform.dto.request.StudentAnswerRequest;
import edu.platform.dto.response.QuizAttemptResponse;
import edu.platform.entity.*;
import edu.platform.entity.enums.QuestionType;
import edu.platform.exception.BusinessException;
import edu.platform.exception.ResourceNotFoundException;
import edu.platform.mapper.QuizAttemptMapper;
import edu.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizAttemptService {
    
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuizAttemptMapper quizAttemptMapper;
    private final CourseVisitService courseVisitService;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public QuizAttemptResponse startQuizAttempt(Long quizId, Long studentId) {
        log.debug("Starting quiz attempt for quiz ID: {} by student ID: {}", quizId, studentId);
        
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + quizId));
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        
        // Check if quiz is active
        if (!quiz.getIsActive()) {
            throw new BusinessException("Quiz is not active");
        }
        
        // Check if student has exceeded maximum attempts
        Long attemptCount = quizAttemptRepository.countByStudentIdAndQuizId(studentId, quizId);
        if (quiz.getMaxAttempts() != null && attemptCount >= quiz.getMaxAttempts()) {
            throw new BusinessException("Maximum attempts exceeded for this quiz");
        }
        
        // Check if there's an active (incomplete) attempt
        Optional<QuizAttempt> activeAttempt = quizAttemptRepository.findActiveAttempt(studentId, quizId);
        if (activeAttempt.isPresent()) {
            log.info("Returning existing active attempt for student ID: {} and quiz ID: {}", studentId, quizId);
            return quizAttemptMapper.toResponse(activeAttempt.get());
        }
        
        // Create new attempt
        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .attemptNumber(attemptCount.intValue() + 1)
                .isCompleted(false)
                .build();
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        courseVisitService.recordQuizStart(studentId, quiz.getCourse().getId(), quizId, null);
        log.info("Quiz attempt started successfully with ID: {}", savedAttempt.getId());
        
        return quizAttemptMapper.toResponse(savedAttempt);
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public QuizAttemptResponse submitQuizAttempt(QuizSubmissionRequest request, Long studentId) {
        log.debug("Submitting quiz attempt for quiz ID: {} by student ID: {}", request.getQuizId(), studentId);
        
        QuizAttempt attempt = quizAttemptRepository.findActiveAttempt(studentId, request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("No active attempt found for this quiz"));
        
        // Check time limit if applicable
        Quiz quiz = attempt.getQuiz();
        if (quiz.getTimeLimitMinutes() != null) {
            LocalDateTime timeLimit = attempt.getStartedAt().plusMinutes(quiz.getTimeLimitMinutes());
            if (LocalDateTime.now().isAfter(timeLimit)) {
                throw new BusinessException("Time limit exceeded for this quiz");
            }
        }
        
        // Get all questions for the quiz
        List<Question> questions = questionRepository.findByQuizIdWithAnswerOptions(request.getQuizId());
        
        // Process student answers
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxPossibleScore = BigDecimal.ZERO;
        
        for (Question question : questions) {
            maxPossibleScore = maxPossibleScore.add(BigDecimal.valueOf(question.getPoints()));
            
            // Find student's answer for this question
            Optional<StudentAnswerRequest> answerRequest = request.getAnswers().stream()
                    .filter(ans -> ans.getQuestionId().equals(question.getId()))
                    .findFirst();
            
            if (answerRequest.isPresent()) {
                StudentAnswer studentAnswer = processStudentAnswer(attempt, question, answerRequest.get());
                studentAnswers.add(studentAnswer);
                
                if (Boolean.TRUE.equals(studentAnswer.getIsCorrect())) {
                    totalScore = totalScore.add(studentAnswer.getPointsEarned());
                }
            }
        }
        
        // Save all student answers
        studentAnswerRepository.saveAll(studentAnswers);
        
        // Update attempt with final score
        attempt.setScore(totalScore);
        attempt.setMaxScore(maxPossibleScore);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setIsCompleted(true);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        Integer durationSeconds = null;
        if (attempt.getStartedAt() != null && attempt.getCompletedAt() != null) {
            durationSeconds = Math.toIntExact(Duration.between(
                    attempt.getStartedAt(), attempt.getCompletedAt()).getSeconds());
            if (durationSeconds < 0) {
                durationSeconds = 0;
            }
        }
        courseVisitService.recordQuizCompletion(
                studentId,
                attempt.getQuiz().getCourse().getId(),
                attempt.getQuiz().getId(),
                durationSeconds,
                null
        );
        log.info("Quiz attempt submitted successfully with ID: {}, Score: {}/{}", 
                savedAttempt.getId(), totalScore, maxPossibleScore);
        
        return quizAttemptMapper.toResponse(savedAttempt);
    }
    
    private StudentAnswer processStudentAnswer(QuizAttempt attempt, Question question, StudentAnswerRequest answerRequest) {
        StudentAnswer studentAnswer = StudentAnswer.builder()
                .attempt(attempt)
                .question(question)
                .build();
        
        boolean isCorrect = false;
        BigDecimal pointsEarned = BigDecimal.ZERO;
        
        if (question.getQuestionType() == QuestionType.TEXT) {
            // For text questions, store the answer but don't auto-grade
            studentAnswer.setAnswerText(answerRequest.getAnswerText());
            // Text questions require manual grading, so we'll mark as correct for now
            // In a real system, this would be handled by teachers
            isCorrect = true;
            pointsEarned = BigDecimal.valueOf(question.getPoints());
        } else {
            // For choice questions, check against correct answers
            List<Long> selectedOptionIds = answerRequest.getSelectedOptionIds();
            if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
                studentAnswer.setSelectedOptionIds(selectedOptionIds);
                
                List<AnswerOption> correctOptions = answerOptionRepository
                        .findCorrectAnswersByQuestionId(question.getId());
                
                if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
                    // For single choice, check if the selected option is correct
                    if (selectedOptionIds.size() == 1) {
                        Long selectedId = selectedOptionIds.get(0);
                        isCorrect = correctOptions.stream()
                                .anyMatch(option -> option.getId().equals(selectedId));
                    }
                } else if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                    // For multiple choice, check if all selected options are correct
                    // and all correct options are selected
                    List<Long> correctOptionIds = correctOptions.stream()
                            .map(AnswerOption::getId)
                            .toList();
                    
                    isCorrect = selectedOptionIds.size() == correctOptionIds.size() &&
                               selectedOptionIds.containsAll(correctOptionIds);
                }
                
                if (isCorrect) {
                    pointsEarned = BigDecimal.valueOf(question.getPoints());
                }
            }
        }
        
        studentAnswer.setIsCorrect(isCorrect);
        studentAnswer.setPointsEarned(pointsEarned);
        
        return studentAnswer;
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<QuizAttemptResponse> getStudentAttempts(Long studentId) {
        log.debug("Fetching quiz attempts for student ID: {}", studentId);
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByStudentId(studentId);
        return quizAttemptMapper.toResponseList(attempts);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<QuizAttemptResponse> getQuizAttempts(Long quizId) {
        log.debug("Fetching attempts for quiz ID: {}", quizId);
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        return quizAttemptMapper.toResponseList(attempts);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public QuizAttemptResponse getAttemptById(Long attemptId) {
        log.debug("Fetching quiz attempt by ID: {}", attemptId);
        
        QuizAttempt attempt = quizAttemptRepository.findByIdWithDetails(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found with ID: " + attemptId));
        
        return quizAttemptMapper.toResponse(attempt);
    }
}
