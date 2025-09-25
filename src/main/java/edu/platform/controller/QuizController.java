package edu.platform.controller;

import edu.platform.dto.request.QuizRequest;
import edu.platform.dto.request.QuizSubmissionRequest;
import edu.platform.dto.response.QuizAttemptResponse;
import edu.platform.dto.response.QuizResponse;
import edu.platform.entity.User;
import edu.platform.service.QuizAttemptService;
import edu.platform.service.QuizService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Quiz Management", description = "Quiz management APIs")
@SecurityRequirement(name = "bearerAuth")
public class QuizController {
    
    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new quiz", description = "Create a new quiz for a course (TEACHER or ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quiz created successfully",
                content = @Content(schema = @Schema(implementation = QuizResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Quiz creation request received from: {}", email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        QuizResponse quiz = quizService.createQuiz(request, currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(quiz);
    }
    
    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get quizzes by course", description = "Get all active quizzes for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quizzes retrieved successfully")
    })
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        log.debug("Getting quizzes for course ID: {}", courseId);
        
        List<QuizResponse> quizzes = quizService.getQuizzesByCourse(courseId);
        return ResponseEntity.ok(quizzes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get quiz by ID", description = "Get quiz details by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz found",
                content = @Content(schema = @Schema(implementation = QuizResponse.class))),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<QuizResponse> getQuizById(
            @Parameter(description = "Quiz ID") @PathVariable Long id) {
        log.debug("Getting quiz by ID: {}", id);
        
        QuizResponse quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Update quiz", description = "Update quiz (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz updated successfully",
                content = @Content(schema = @Schema(implementation = QuizResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<QuizResponse> updateQuiz(
            @Parameter(description = "Quiz ID") @PathVariable Long id,
            @Valid @RequestBody QuizRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Quiz update request received for ID: {} from: {}", id, email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        QuizResponse quiz = quizService.updateQuiz(id, request, currentUser.getId());
        
        return ResponseEntity.ok(quiz);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Delete quiz", description = "Delete quiz (only by course teacher or ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<Void> deleteQuiz(
            @Parameter(description = "Quiz ID") @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Quiz deletion request received for ID: {} from: {}", id, email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        quizService.deleteQuiz(id, currentUser.getId());
        
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get my quizzes", description = "Get quizzes created by current teacher")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quizzes retrieved successfully")
    })
    public ResponseEntity<List<QuizResponse>> getMyQuizzes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.debug("Getting quizzes for teacher: {}", email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        List<QuizResponse> quizzes = quizService.getQuizzesByTeacher(currentUser.getId());
        
        return ResponseEntity.ok(quizzes);
    }
    
    // Quiz Attempt Endpoints
    
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Start quiz attempt", description = "Start a new quiz attempt (STUDENT only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Quiz attempt started successfully",
                content = @Content(schema = @Schema(implementation = QuizAttemptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Cannot start quiz attempt"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<QuizAttemptResponse> startQuizAttempt(
            @Parameter(description = "Quiz ID") @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Quiz attempt start request for quiz ID: {} from: {}", id, email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        QuizAttemptResponse attempt = quizAttemptService.startQuizAttempt(id, currentUser.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(attempt);
    }
    
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Submit quiz attempt", description = "Submit quiz attempt with answers (STUDENT only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz submitted successfully",
                content = @Content(schema = @Schema(implementation = QuizAttemptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid submission"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<QuizAttemptResponse> submitQuiz(
            @Valid @RequestBody QuizSubmissionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.info("Quiz submission request for quiz ID: {} from: {}", request.getQuizId(), email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        QuizAttemptResponse attempt = quizAttemptService.submitQuizAttempt(request, currentUser.getId());
        
        return ResponseEntity.ok(attempt);
    }
    
    @GetMapping("/attempts/my")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @Operation(summary = "Get my quiz attempts", description = "Get all quiz attempts by current student")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz attempts retrieved successfully")
    })
    public ResponseEntity<List<QuizAttemptResponse>> getMyAttempts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        
        log.debug("Getting quiz attempts for student: {}", email);
        
        User currentUser = userService.getCurrentUserEntity(email);
        List<QuizAttemptResponse> attempts = quizAttemptService.getStudentAttempts(currentUser.getId());
        
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/{id}/attempts")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get quiz attempts", description = "Get all attempts for a specific quiz (TEACHER or ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz attempts retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    public ResponseEntity<List<QuizAttemptResponse>> getQuizAttempts(
            @Parameter(description = "Quiz ID") @PathVariable Long id) {
        log.debug("Getting attempts for quiz ID: {}", id);
        
        List<QuizAttemptResponse> attempts = quizAttemptService.getQuizAttempts(id);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/attempts/{attemptId}")
    @Operation(summary = "Get quiz attempt details", description = "Get detailed information about a quiz attempt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quiz attempt found",
                content = @Content(schema = @Schema(implementation = QuizAttemptResponse.class))),
        @ApiResponse(responseCode = "404", description = "Quiz attempt not found")
    })
    public ResponseEntity<QuizAttemptResponse> getAttemptById(
            @Parameter(description = "Attempt ID") @PathVariable Long attemptId) {
        log.debug("Getting quiz attempt by ID: {}", attemptId);
        
        QuizAttemptResponse attempt = quizAttemptService.getAttemptById(attemptId);
        return ResponseEntity.ok(attempt);
    }
}
