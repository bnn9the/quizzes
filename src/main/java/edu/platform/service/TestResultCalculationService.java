package edu.platform.service;

import edu.platform.dto.request.TestResultCalculationRequest;
import edu.platform.entity.*;
import edu.platform.entity.enums.TestResultStatus;
import edu.platform.exception.ResourceNotFoundException;
import edu.platform.repository.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for calculating test results with circuit breaker protection.
 * This service is isolated from the main course/quiz functionality to prevent
 * cascading failures.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultCalculationService {
    
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final QuestionRepository questionRepository;
    private final TestResultRepository testResultRepository;

    /**
     * Calculate test result with circuit breaker and retry protection.
     * If calculation fails, the error won't block the quiz system.
     */
    @CircuitBreaker(name = "testResultCalculation", fallbackMethod = "calculateResultFallback")
    @Retry(name = "testResultCalculation")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public TestResult calculateResult(TestResultCalculationRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("Calculating test result for quiz attempt: {}", request.getQuizAttemptId());
        
        // Check if result already exists
        if (!Boolean.TRUE.equals(request.getForceRecalculation())) {
            TestResult existing = testResultRepository.findByQuizAttemptId(request.getQuizAttemptId())
                    .orElse(null);
            if (existing != null) {
                log.info("Test result already exists for attempt: {}", request.getQuizAttemptId());
                return existing;
            }
        }
        
        // Load quiz attempt
        QuizAttempt attempt = quizAttemptRepository.findByIdWithDetails(request.getQuizAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Quiz attempt not found with ID: " + request.getQuizAttemptId()));
        
        if (!Boolean.TRUE.equals(attempt.getIsCompleted())) {
            throw new IllegalStateException("Cannot calculate result for incomplete attempt");
        }
        
        // Load all student answers
        List<StudentAnswer> answers = studentAnswerRepository.findByAttemptIdWithQuestions(
            request.getQuizAttemptId());
        
        // Load all questions for the quiz
        List<Question> questions = questionRepository.findByQuizIdWithAnswerOptions(
            attempt.getQuiz().getId());
        
        // Calculate scores
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxPossibleScore = BigDecimal.ZERO;
        int correctAnswersCount = 0;
        
        for (Question question : questions) {
            maxPossibleScore = maxPossibleScore.add(BigDecimal.valueOf(question.getPoints()));
            
            // Find corresponding student answer
            StudentAnswer studentAnswer = answers.stream()
                    .filter(ans -> ans.getQuestion().getId().equals(question.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (studentAnswer != null && Boolean.TRUE.equals(studentAnswer.getIsCorrect())) {
                totalScore = totalScore.add(studentAnswer.getPointsEarned());
                correctAnswersCount++;
            }
        }
        
        // Calculate percentage
        BigDecimal percentage = BigDecimal.ZERO;
        if (maxPossibleScore.compareTo(BigDecimal.ZERO) > 0) {
            percentage = totalScore
                    .multiply(BigDecimal.valueOf(100))
                    .divide(maxPossibleScore, 2, RoundingMode.HALF_UP);
        }
        
        // Determine status
        TestResultStatus status;
        BigDecimal passingScore = request.getPassingScore() != null 
            ? request.getPassingScore() 
            : BigDecimal.valueOf(70); // Default 70%
        
        if (percentage.compareTo(passingScore) >= 0) {
            status = TestResultStatus.PASSED;
        } else {
            status = TestResultStatus.FAILED;
        }
        
        // Calculate time spent
        Long timeSpentSeconds = null;
        if (attempt.getStartedAt() != null && attempt.getCompletedAt() != null) {
            Duration duration = Duration.between(attempt.getStartedAt(), attempt.getCompletedAt());
            timeSpentSeconds = duration.getSeconds();
        }
        
        // Create test result
        TestResult testResult = TestResult.builder()
                .quizAttempt(attempt)
                .student(attempt.getStudent())
                .quiz(attempt.getQuiz())
                .score(totalScore)
                .maxScore(maxPossibleScore)
                .percentage(percentage)
                .passingScore(passingScore)
                .status(status)
                .timeSpentSeconds(timeSpentSeconds)
                .startedAt(attempt.getStartedAt())
                .completedAt(attempt.getCompletedAt())
                .correctAnswers(correctAnswersCount)
                .totalQuestions(questions.size())
                .calculationTimeMs(System.currentTimeMillis() - startTime)
                .build();
        
        TestResult saved = testResultRepository.save(testResult);
        
        log.info("Test result calculated successfully: attempt={}, score={}/{}, percentage={}%, status={}, time={}ms",
                request.getQuizAttemptId(), totalScore, maxPossibleScore, percentage, status,
                saved.getCalculationTimeMs());
        
        return saved;
    }
    
    /**
     * Fallback method when calculation fails or circuit is open.
     * Returns an ERROR status result instead of failing completely.
     */
    private TestResult calculateResultFallback(TestResultCalculationRequest request, Exception e) {
        log.error("Failed to calculate test result for attempt: {}. Reason: {}", 
                request.getQuizAttemptId(), e.getMessage(), e);
        
        try {
            // Try to load basic data
            QuizAttempt attempt = quizAttemptRepository.findById(request.getQuizAttemptId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz attempt not found with ID: " + request.getQuizAttemptId()));
            
            // Create error result
            TestResult errorResult = TestResult.builder()
                    .quizAttempt(attempt)
                    .student(attempt.getStudent())
                    .quiz(attempt.getQuiz())
                    .score(BigDecimal.ZERO)
                    .maxScore(attempt.getMaxScore() != null ? attempt.getMaxScore() : BigDecimal.ZERO)
                    .percentage(BigDecimal.ZERO)
                    .status(TestResultStatus.ERROR)
                    .startedAt(attempt.getStartedAt())
                    .completedAt(attempt.getCompletedAt())
                    .errorMessage("Calculation service unavailable: " + e.getMessage())
                    .build();
            
            return testResultRepository.save(errorResult);
        } catch (Exception fallbackException) {
            log.error("Fallback also failed for attempt: {}", request.getQuizAttemptId(), fallbackException);
            throw new RuntimeException("Test result calculation completely failed", fallbackException);
        }
    }
    
    /**
     * Mark timed-out attempts as TIMEOUT status
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTimedOutAttempts(int timeoutMinutes) {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<TestResult> timedOut = testResultRepository.findTimedOutAttempts(timeout);
        
        for (TestResult result : timedOut) {
            result.setStatus(TestResultStatus.TIMEOUT);
            result.setCompletedAt(LocalDateTime.now());
            result.setErrorMessage("Test timed out after " + timeoutMinutes + " minutes");
            testResultRepository.save(result);
        }
        
        if (!timedOut.isEmpty()) {
            log.info("Marked {} test attempts as timed out", timedOut.size());
        }
    }
}