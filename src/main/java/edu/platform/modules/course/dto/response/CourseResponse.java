package edu.platform.modules.course.dto.response;

import edu.platform.modules.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Course response")
public class CourseResponse {
    
    @Schema(description = "Course ID", example = "1")
    private Long id;
    
    @Schema(description = "Course title", example = "Introduction to Spring Boot")
    private String title;
    
    @Schema(description = "Course description", example = "Learn the basics of Spring Boot framework")
    private String description;
    
    @Schema(description = "Teacher information")
    private UserResponse teacher;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
