package edu.platform.modules.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Test result calculation request")
public class TestResultCalculationRequest {
    
    @NotNull(message = "Quiz attempt ID is required")
    @Positive(message = "Quiz attempt ID must be positive")
    @Schema(description = "Quiz attempt ID", example = "1")
    private Long quizAttemptId;
    
    @Schema(description = "Passing score percentage (e.g., 70.00 for 70%)", example = "70.00")
    private BigDecimal passingScore;
    
    @Schema(description = "Force recalculation even if result exists", example = "false")
    private Boolean forceRecalculation;
}