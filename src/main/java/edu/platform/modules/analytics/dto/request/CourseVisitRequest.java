package edu.platform.modules.analytics.dto.request;

import edu.platform.modules.analytics.enums.VisitType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course visit creation request")
public class CourseVisitRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID that performed the action", example = "42")
    private Long userId;

    @NotNull(message = "Course ID is required")
    @Schema(description = "Course ID related to the visit", example = "7")
    private Long courseId;

    @Schema(description = "Lesson ID if the visit is lesson-related", example = "15")
    private Long lessonId;

    @Schema(description = "Quiz ID if the visit is quiz-related", example = "3")
    private Long quizId;

    @NotNull(message = "Visit type is required")
    @Schema(description = "Visit type", example = "LESSON_VIEW")
    private VisitType visitType;

    @PositiveOrZero(message = "Duration must be zero or positive")
    @Schema(description = "Duration in seconds spent on the page", example = "180")
    private Integer durationSeconds;

    @Size(max = 50, message = "Device type must not exceed 50 characters")
    @Schema(description = "Client device type", example = "DESKTOP")
    private String deviceType;

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Schema(description = "User IP address", example = "192.168.0.10")
    private String ipAddress;

    @Size(max = 500, message = "User-Agent must not exceed 500 characters")
    @Schema(description = "HTTP User-Agent header", example = "Mozilla/5.0 ...")
    private String userAgent;

    @Schema(description = "Timestamp of the visit. Defaults to current time if omitted")
    private LocalDateTime visitedAt;

}

