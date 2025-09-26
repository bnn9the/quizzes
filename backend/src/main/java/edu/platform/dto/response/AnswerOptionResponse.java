package edu.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Answer option response")
public class AnswerOptionResponse {
    
    @Schema(description = "Option ID", example = "1")
    private Long id;
    
    @Schema(description = "Option text", example = "A Java framework")
    private String optionText;
    
    @Schema(description = "Option order", example = "1")
    private Integer orderIndex;
    
    // Note: isCorrect is not included in response for security reasons
}
