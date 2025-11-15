package edu.platform.modules.quiz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Student answer request")
public class StudentAnswerRequest {
    
    @NotNull(message = "Question ID is required")
    @Schema(description = "Question ID", example = "1")
    private Long questionId;
    
    @Schema(description = "Text answer (for text questions)", example = "Spring Boot is a Java framework")
    private String answerText;
    
    @Schema(description = "Selected option IDs (for choice questions)", example = "[1, 3]")
    private List<Long> selectedOptionIds;
}
