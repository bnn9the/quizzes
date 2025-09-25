package edu.platform.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Answer option request")
public class AnswerOptionRequest {
    
    @NotBlank(message = "Option text is required")
    @Schema(description = "Option text", example = "A Java framework")
    private String optionText;
    
    @NotNull(message = "Is correct flag is required")
    @Schema(description = "Whether this option is correct", example = "true")
    private Boolean isCorrect;
    
    @NotNull(message = "Order index is required")
    @Schema(description = "Option order", example = "1")
    private Integer orderIndex;
}
