package edu.platform.modules.quiz.dto.response;

import edu.platform.modules.course.dto.response.CourseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Quiz response")
public class QuizResponse {
    
    @Schema(description = "Quiz ID", example = "1")
    private Long id;
    
    @Schema(description = "Quiz title", example = "Spring Boot Basics Quiz")
    private String title;
    
    @Schema(description = "Quiz description", example = "Test your knowledge of Spring Boot fundamentals")
    private String description;
    
    @Schema(description = "Course information")
    private CourseResponse course;
    
    @Schema(description = "Maximum number of attempts allowed", example = "3")
    private Integer maxAttempts;
    
    @Schema(description = "Time limit in minutes", example = "30")
    private Integer timeLimitMinutes;
    
    @Schema(description = "Whether the quiz is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "List of questions")
    private List<QuestionResponse> questions;
}
