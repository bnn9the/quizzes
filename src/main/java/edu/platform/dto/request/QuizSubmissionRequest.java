package edu.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Quiz submission request")
public class QuizSubmissionRequest {
    
    @NotNull(message = "Quiz ID is required")
    @Schema(description = "Quiz ID", example = "1")
    private Long quizId;
    
    @Valid
    @NotEmpty(message = "Answers are required")
    @Schema(description = "Student answers")
    private List<StudentAnswerRequest> answers;
}
