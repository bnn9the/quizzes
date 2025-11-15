package edu.platform.modules.quiz.dto.response;

import edu.platform.modules.quiz.enums.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Question response")
public class QuestionResponse {
    
    @Schema(description = "Question ID", example = "1")
    private Long id;
    
    @Schema(description = "Question text", example = "What is Spring Boot?")
    private String questionText;
    
    @Schema(description = "Type of question", example = "SINGLE_CHOICE")
    private QuestionType questionType;
    
    @Schema(description = "Points for correct answer", example = "5")
    private Integer points;
    
    @Schema(description = "Question order in quiz", example = "1")
    private Integer orderIndex;
    
    @Schema(description = "Answer options")
    private List<AnswerOptionResponse> answerOptions;
}

