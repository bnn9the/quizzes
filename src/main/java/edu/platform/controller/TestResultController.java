package edu.platform.controller;

import edu.platform.dto.request.TestResultCalculationRequest;
import edu.platform.dto.response.TestResultResponse;
import edu.platform.entity.enums.TestResultStatus;
import edu.platform.service.TestResultService;
import edu.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test-results")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Test Results", description = "Test result management APIs with circuit breaker protection")
@SecurityRequirement(name = "bearerAuth")
public class TestResultController {
    
    private final TestResultService testResultService;
    private final UserService userService;
    
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Calculate test result", 
               description = "Calculate test result for a quiz attempt (with circuit breaker protection)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Result calculated successfully",
                content = @Content(schema = @Schema(implementation = TestResultResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or attempt not completed"),
        @ApiResponse(responseCode = "503", description = "Calculation service unavailable (circuit breaker open)")
    })
    public ResponseEntity<TestResultResponse> calculateResult(
            @Valid @RequestBody TestResultCalculationRequest request) {
        
        log.info("Calculate test result request for attempt: {}", request.getQuizAttemptId());
        
        TestResultResponse response = testResultService.calculateAndSaveResult(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get test result by ID", description = "Get test result details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result found",
                content = @Content(schema = @Schema(implementation = TestResultResponse.class))),
        @ApiResponse(responseCode = "404", description = "Result not found")
    })
    public ResponseEntity<TestResultResponse> getResultById(
            @Parameter(description = "Result ID") @PathVariable Long id) {
        
        log.debug("Get test result by ID: {}", id);
        
        TestResultResponse response = testResultService.getResultById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/attempt/{attemptId}")
    @Operation(summary = "Get result by attempt ID", description = "Get test result by quiz attempt ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Result found"),
        @ApiResponse(responseCode = "404", description = "Result not found for this attempt")
    })
    public ResponseEntity<TestResultResponse> getResultByAttemptId(
            @Parameter(description = "Quiz attempt ID") @PathVariable Long attemptId) {
        
        log.debug("Get test result by attempt ID: {}", attemptId);
        
        TestResultResponse response = testResultService.getResultByAttemptId(attemptId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Get my test results", description = "Get all test results for current student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getMyResults() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long studentId = userService.getCurrentUserEntity(email).getId();
        
        log.debug("Get test results for student: {}", email);
        
        List<TestResultResponse> results = testResultService.getResultsByStudent(studentId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get results by student", description = "Get all test results for a specific student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getResultsByStudent(
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        
        log.debug("Get test results for student ID: {}", studentId);
        
        List<TestResultResponse> results = testResultService.getResultsByStudent(studentId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get results by quiz", description = "Get all test results for a specific quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getResultsByQuiz(
            @Parameter(description = "Quiz ID") @PathVariable Long quizId) {
        
        log.debug("Get test results for quiz ID: {}", quizId);
        
        List<TestResultResponse> results = testResultService.getResultsByQuiz(quizId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/quiz/{quizId}/student/{studentId}")
    @Operation(summary = "Get results by student and quiz", 
               description = "Get all test results for a specific student and quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getResultsByStudentAndQuiz(
            @Parameter(description = "Quiz ID") @PathVariable Long quizId,
            @Parameter(description = "Student ID") @PathVariable Long studentId) {
        
        log.debug("Get test results for student: {} and quiz: {}", studentId, quizId);
        
        List<TestResultResponse> results = testResultService.getResultsByStudentAndQuiz(studentId, quizId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get results by status", description = "Get all test results with specific status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getResultsByStatus(
            @Parameter(description = "Status", example = "PASSED") 
            @PathVariable TestResultStatus status) {
        
        log.debug("Get test results by status: {}", status);
        
        List<TestResultResponse> results = testResultService.getResultsByStatus(status);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/quiz/{quizId}/top-scores")
    @Operation(summary = "Get top scores", description = "Get top scoring results for a quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top scores retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getTopScores(
            @Parameter(description = "Quiz ID") @PathVariable Long quizId) {
        
        log.debug("Get top scores for quiz: {}", quizId);
        
        List<TestResultResponse> results = testResultService.getTopScoresByQuiz(quizId);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/quiz/{quizId}/statistics")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get quiz statistics", description = "Get aggregated statistics for a quiz")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getQuizStatistics(
            @Parameter(description = "Quiz ID") @PathVariable Long quizId) {
        
        log.debug("Get statistics for quiz: {}", quizId);
        
        Map<String, Object> statistics = testResultService.getQuizStatistics(quizId);
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get results in date range", 
               description = "Get all test results completed within a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Results retrieved successfully")
    })
    public ResponseEntity<List<TestResultResponse>> getResultsInDateRange(
            @Parameter(description = "Start date (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.debug("Get test results between {} and {}", startDate, endDate);
        
        List<TestResultResponse> results = testResultService.getResultsInDateRange(startDate, endDate);
        return ResponseEntity.ok(results);
    }
}