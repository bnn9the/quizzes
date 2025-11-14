package edu.platform.modules.quiz.service;

import edu.platform.modules.quiz.dto.request.TestResultCalculationRequest;
import edu.platform.modules.quiz.dto.response.TestResultResponse;
import edu.platform.modules.quiz.entity.TestResult;
import edu.platform.modules.quiz.enums.TestResultStatus;
import edu.platform.common.exception.ResourceNotFoundException;
import edu.platform.modules.quiz.mapper.TestResultMapper;
import edu.platform.modules.quiz.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultService {
    
    private final TestResultRepository testResultRepository;
    private final TestResultCalculationService calculationService;
    private final TestResultMapper testResultMapper;
    
    /**
     * Calculate and save test result.
     * Uses isolated calculation service with circuit breaker protection.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TestResultResponse calculateAndSaveResult(TestResultCalculationRequest request) {
        log.debug("Initiating test result calculation for attempt: {}", request.getQuizAttemptId());
        
        TestResult result = calculationService.calculateResult(request);
        return testResultMapper.toResponse(result);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public TestResultResponse getResultById(Long id) {
        log.debug("Fetching test result by ID: {}", id);
        
        TestResult result = testResultRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test result not found with ID: " + id));
        
        return testResultMapper.toResponse(result);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public TestResultResponse getResultByAttemptId(Long attemptId) {
        log.debug("Fetching test result by quiz attempt ID: {}", attemptId);
        
        TestResult result = testResultRepository.findByQuizAttemptId(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Test result not found for quiz attempt ID: " + attemptId));
        
        return testResultMapper.toResponse(result);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getResultsByStudent(Long studentId) {
        log.debug("Fetching test results for student: {}", studentId);
        
        List<TestResult> results = testResultRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        return testResultMapper.toResponseList(results);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getResultsByQuiz(Long quizId) {
        log.debug("Fetching test results for quiz: {}", quizId);
        
        List<TestResult> results = testResultRepository.findByQuizIdOrderByCreatedAtDesc(quizId);
        return testResultMapper.toResponseList(results);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getResultsByStudentAndQuiz(Long studentId, Long quizId) {
        log.debug("Fetching test results for student: {} and quiz: {}", studentId, quizId);
        
        List<TestResult> results = testResultRepository.findByStudentIdAndQuizId(studentId, quizId);
        return testResultMapper.toResponseList(results);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getResultsByStatus(TestResultStatus status) {
        log.debug("Fetching test results by status: {}", status);
        
        List<TestResult> results = testResultRepository.findByStatus(status);
        return testResultMapper.toResponseList(results);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getTopScoresByQuiz(Long quizId) {
        log.debug("Fetching top scores for quiz: {}", quizId);
        
        List<TestResult> results = testResultRepository.findTopScoresByQuizId(quizId);
        return testResultMapper.toResponseList(results);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public Map<String, Object> getQuizStatistics(Long quizId) {
        log.debug("Calculating statistics for quiz: {}", quizId);
        
        Double averagePercentage = testResultRepository.getAveragePercentageByQuizId(quizId);
        Long passedCount = testResultRepository.countPassedByQuizId(quizId);
        Long failedCount = testResultRepository.countFailedByQuizId(quizId);
        
        return Map.of(
            "quizId", quizId,
            "averagePercentage", averagePercentage != null ? averagePercentage : 0.0,
            "passedCount", passedCount,
            "failedCount", failedCount,
            "totalAttempts", passedCount + failedCount
        );
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<TestResultResponse> getResultsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching test results between {} and {}", startDate, endDate);
        
        List<TestResult> results = testResultRepository.findCompletedBetween(startDate, endDate);
        return testResultMapper.toResponseList(results);
    }
    
    /**
     * Check if result exists for attempt
     */
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public boolean resultExistsForAttempt(Long attemptId) {
        return testResultRepository.existsByQuizAttemptId(attemptId);
    }
    
    /**
     * Mark old in-progress attempts as timed out
     */
    @Transactional
    public void processTimedOutAttempts(int timeoutMinutes) {
        log.info("Processing timed out test attempts (timeout: {} minutes)", timeoutMinutes);
        calculationService.markTimedOutAttempts(timeoutMinutes);
    }
}



