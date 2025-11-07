package edu.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Aggregated course statistics response")
public class CourseStatisticsResponse {

    @Schema(description = "Course ID", example = "7")
    private Long courseId;

    @Schema(description = "Total visits for the period", example = "1250")
    private Long totalVisits;

    @Schema(description = "Unique visitors for the period", example = "320")
    private Long uniqueVisitors;

    @Schema(description = "Total time spent in seconds", example = "45000")
    private Long totalDurationSeconds;

    @Schema(description = "Average duration in seconds", example = "180")
    private Integer averageDurationSeconds;

    @Schema(description = "Number of lesson views", example = "800")
    private Long lessonViews;

    @Schema(description = "Number of quiz attempts", example = "120")
    private Long quizAttempts;

    @Schema(description = "Completion rate percentage", example = "62.5")
    private Double completionRate;

    @Schema(description = "Aggregation period type", example = "DAILY")
    private String periodType;

    @Schema(description = "Aggregation period start date")
    private LocalDate periodStart;

    @Schema(description = "Aggregation period end date")
    private LocalDate periodEnd;

    @Schema(description = "Timestamp when statistics were last aggregated")
    private LocalDateTime lastAggregatedAt;
}
