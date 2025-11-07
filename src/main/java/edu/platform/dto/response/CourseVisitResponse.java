package edu.platform.dto.response;

import edu.platform.entity.enums.VisitType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course visit response")
public class CourseVisitResponse {

    @Schema(description = "Visit ID", example = "100")
    private Long id;

    @Schema(description = "User ID", example = "42")
    private Long userId;

    @Schema(description = "Course ID", example = "7")
    private Long courseId;

    @Schema(description = "Lesson ID (if applicable)", example = "15")
    private Long lessonId;

    @Schema(description = "Quiz ID (if applicable)", example = "3")
    private Long quizId;

    @Schema(description = "Visit type", example = "LESSON_VIEW")
    private VisitType visitType;

    @Schema(description = "Duration in seconds", example = "180")
    private Integer durationSeconds;

    @Schema(description = "Device type", example = "DESKTOP")
    private String deviceType;

    @Schema(description = "IP address", example = "192.168.0.10")
    private String ipAddress;

    @Schema(description = "User-Agent header")
    private String userAgent;

    @Schema(description = "Visit timestamp")
    private LocalDateTime visitedAt;

    @Schema(description = "Record creation timestamp")
    private LocalDateTime createdAt;
}
