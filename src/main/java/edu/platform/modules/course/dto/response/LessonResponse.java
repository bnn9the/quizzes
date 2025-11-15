package edu.platform.modules.course.dto.response;

import edu.platform.modules.media.dto.response.MediaAssetResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Lesson response")
public class LessonResponse {
    
    @Schema(description = "Lesson ID", example = "1")
    private Long id;
    
    @Schema(description = "Course ID", example = "1")
    private Long courseId;
    
    @Schema(description = "Course title", example = "Spring Boot Fundamentals")
    private String courseTitle;
    
    @Schema(description = "Lesson title", example = "Introduction to Spring Boot")
    private String title;
    
    @Schema(description = "Lesson content", example = "In this lesson we will learn...")
    private String content;
    
    @Schema(description = "Lesson order in course", example = "1")
    private Integer orderIndex;
    
    @Schema(description = "Version for optimistic locking", example = "0")
    private Long version;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Attached media assets")
    private List<MediaAssetResponse> mediaAssets;
}
