package edu.platform.modules.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Quiz creation request")
public class QuizRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Quiz title", example = "Spring Boot Basics Quiz")
    private String title;
    
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Schema(description = "Quiz description", example = "Test your knowledge of Spring Boot fundamentals")
    private String description;
    
    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be positive")
    @Schema(description = "Course ID", example = "1")
    private Long courseId;
    
    @Positive(message = "Max attempts must be positive")
    @Schema(description = "Maximum number of attempts allowed", example = "3")
    private Integer maxAttempts = 1;
    
    @Positive(message = "Time limit must be positive")
    @Schema(description = "Time limit in minutes", example = "30")
    private Integer timeLimitMinutes;
    
    @Valid
    @Size(min = 1, message = "At least one question is required")
    @Schema(description = "List of questions")
    private List<QuestionRequest> questions;
}
