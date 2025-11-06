package edu.platform.dto.response;

import edu.platform.entity.enums.TestResultStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Test result response")
public class TestResultResponse {
    
    @Schema(description = "Result ID", example = "1")
    private Long id;
    
    @Schema(description = "Quiz attempt ID", example = "1")
    private Long quizAttemptId;
    
    @Schema(description = "Student information")
    private UserResponse student;
    
    @Schema(description = "Quiz information")
    private QuizResponse quiz;
    
    @Schema(description = "Score achieved", example = "85.50")
    private BigDecimal score;
    
    @Schema(description = "Maximum possible score", example = "100.00")
    private BigDecimal maxScore;
    
    @Schema(description = "Percentage achieved", example = "85.50")
    private BigDecimal percentage;
    
    @Schema(description = "Passing score percentage", example = "70.00")
    private BigDecimal passingScore;
    
    @Schema(description = "Result status", example = "PASSED")
    private TestResultStatus status;
    
    @Schema(description = "Time spent in seconds", example = "1800")
    private Long timeSpentSeconds;
    
    @Schema(description = "Formatted time spent", example = "30m 0s")
    private String formattedTimeSpent;
    
    @Schema(description = "Test started at")
    private LocalDateTime startedAt;
    
    @Schema(description = "Test completed at")
    private LocalDateTime completedAt;
    
    @Schema(description = "Number of correct answers", example = "8")
    private Integer correctAnswers;
    
    @Schema(description = "Total number of questions", example = "10")
    private Integer totalQuestions;
    
    @Schema(description = "Calculation time in milliseconds", example = "150")
    private Long calculationTimeMs;
    
    @Schema(description = "Error message if status is ERROR")
    private String errorMessage;
    
    @Schema(description = "Whether test was passed", example = "true")
    private Boolean passed;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}