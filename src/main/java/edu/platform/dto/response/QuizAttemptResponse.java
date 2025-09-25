package edu.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Quiz attempt response")
public class QuizAttemptResponse {
    
    @Schema(description = "Attempt ID", example = "1")
    private Long id;
    
    @Schema(description = "Quiz information")
    private QuizResponse quiz;
    
    @Schema(description = "Student information")
    private UserResponse student;
    
    @Schema(description = "Attempt number", example = "1")
    private Integer attemptNumber;
    
    @Schema(description = "Score achieved", example = "85.5")
    private BigDecimal score;
    
    @Schema(description = "Maximum possible score", example = "100.0")
    private BigDecimal maxScore;
    
    @Schema(description = "Start timestamp")
    private LocalDateTime startedAt;
    
    @Schema(description = "Completion timestamp")
    private LocalDateTime completedAt;
    
    @Schema(description = "Whether the attempt is completed", example = "true")
    private Boolean isCompleted;
}
