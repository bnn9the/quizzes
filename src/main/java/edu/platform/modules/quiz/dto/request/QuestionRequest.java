package edu.platform.modules.quiz.dto.request;

import edu.platform.modules.quiz.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Question creation request")
public class QuestionRequest {
    
    @NotBlank(message = "Question text is required")
    @Schema(description = "Question text", example = "What is Spring Boot?")
    private String questionText;
    
    @NotNull(message = "Question type is required")
    @Schema(description = "Type of question", example = "SINGLE_CHOICE")
    private QuestionType questionType;
    
    @Positive(message = "Points must be positive")
    @Schema(description = "Points for correct answer", example = "5")
    private Integer points = 1;
    
    @NotNull(message = "Order index is required")
    @Schema(description = "Question order in quiz", example = "1")
    private Integer orderIndex;
    
    @Valid
    @Schema(description = "Answer options (for choice questions)")
    private List<AnswerOptionRequest> answerOptions;
}

