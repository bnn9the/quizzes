package edu.platform.modules.course.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Lesson creation/update request")
public class LessonRequest {
    
    @NotNull(message = "Course ID is required")
    @Positive(message = "Course ID must be positive")
    @Schema(description = "Course ID", example = "1")
    private Long courseId;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Schema(description = "Lesson title", example = "Introduction to Spring Boot")
    private String title;
    
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    @Schema(description = "Lesson content (text/markdown)", example = "In this lesson we will learn...")
    private String content;
    
    @NotNull(message = "Order index is required")
    @Schema(description = "Lesson order in course", example = "1")
    private Integer orderIndex;
    
    @Schema(description = "Media asset IDs to attach to this lesson", example = "[1, 2, 3]")
    private List<Long> mediaAssetIds;
}