package edu.platform.modules.analytics.controller;

import edu.platform.modules.analytics.dto.request.CourseVisitRequest;
import edu.platform.modules.analytics.dto.response.CourseStatisticsResponse;
import edu.platform.modules.analytics.dto.response.CourseVisitResponse;
import edu.platform.modules.analytics.enums.VisitType;
import edu.platform.modules.analytics.mapper.CourseVisitMapper;
import edu.platform.modules.analytics.repository.CourseVisitRepository;
import edu.platform.modules.analytics.service.CourseVisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course Statistics", description = "Course activity and engagement statistics APIs")
@SecurityRequirement(name = "bearerAuth")
public class CourseStatisticsController {
    
    private final CourseVisitRepository courseVisitRepository;
    private final CourseVisitService courseVisitService;
    private final CourseVisitMapper courseVisitMapper;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Get overall statistics for a course
     */
    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get course statistics", 
               description = "Get aggregated statistics for a specific course")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                content = @Content(schema = @Schema(implementation = CourseStatisticsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Course not found")
    })
    public ResponseEntity<CourseStatisticsResponse> getCourseStatistics(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        
        log.debug("Getting statistics for course: {}", courseId);
        
        Long totalVisits = courseVisitRepository.countByCourseId(courseId);
        Long uniqueVisitors = courseVisitRepository.countUniqueVisitorsByCourseId(courseId);
        Long totalDuration = courseVisitRepository.getTotalDurationByCourseId(courseId);
        Double avgDuration = courseVisitRepository.getAverageDurationByCourseId(courseId);
        Long lessonViews = courseVisitRepository.countByCourseIdAndVisitType(courseId, VisitType.LESSON_VIEW);
        Long quizStarts = courseVisitRepository.countByCourseIdAndVisitType(courseId, VisitType.QUIZ_START);
        Long quizCompletions = courseVisitRepository.countByCourseIdAndVisitType(courseId, VisitType.QUIZ_COMPLETE);
        Long quizAttempts = courseVisitRepository.countByCourseIdAndVisitType(courseId, VisitType.QUIZ_VIEW);
        Double completionRate = (quizStarts != null && quizStarts > 0 && quizCompletions != null)
                ? (quizCompletions.doubleValue() / quizStarts.doubleValue()) * 100
                : 0.0;
        
        CourseStatisticsResponse response = CourseStatisticsResponse.builder()
                .courseId(courseId)
                .totalVisits(totalVisits)
                .uniqueVisitors(uniqueVisitors)
                .totalDurationSeconds(totalDuration)
                .averageDurationSeconds(avgDuration != null ? avgDuration.intValue() : 0)
                .lessonViews(lessonViews != null ? lessonViews : 0)
                .quizAttempts(quizAttempts != null ? quizAttempts : 0)
                .completionRate(completionRate)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get daily statistics for a course
     */
    @GetMapping("/courses/{courseId}/daily")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get daily statistics", 
               description = "Get daily aggregated statistics for a course")
    public ResponseEntity<List<Map<String, Object>>> getDailyStatistics(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @Parameter(description = "Start date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting daily statistics for course {} from {} to {}", 
                 courseId, startDate, endDate);
        
        String sql = "SELECT " +
                    "period_start as date, " +
                    "total_visits, " +
                    "unique_visitors, " +
                    "total_duration_seconds, " +
                    "avg_duration_seconds, " +
                    "lesson_views, " +
                    "quiz_attempts, " +
                    "completion_rate " +
                    "FROM course_statistics_aggregated " +
                    "WHERE course_id = ? " +
                    "AND period_type = 'DAILY' " +
                    "AND period_start BETWEEN ? AND ? " +
                    "ORDER BY period_start";
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
            sql, courseId, startDate, endDate);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get visit trend for a course
     */
    @GetMapping("/courses/{courseId}/trend")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get visit trend", 
               description = "Get daily visit counts for trend visualization")
    public ResponseEntity<List<Map<String, Object>>> getVisitTrend(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @Parameter(description = "Number of days", example = "30") 
            @RequestParam(defaultValue = "30") Integer days) {
        
        log.debug("Getting visit trend for course {} for last {} days", courseId, days);
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Object[]> trend = courseVisitRepository.getVisitTrendByCourseId(
            courseId, startDate, endDate);
        
        List<Map<String, Object>> results = trend.stream()
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("date", row[0]);
                map.put("visits", row[1]);
                return map;
            })
            .toList();
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get most active users for a course
     */
    @GetMapping("/courses/{courseId}/active-users")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get most active users", 
               description = "Get users with most visits to the course")
    public ResponseEntity<List<Map<String, Object>>> getMostActiveUsers(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @Parameter(description = "Limit", example = "10") 
            @RequestParam(defaultValue = "10") Integer limit) {
        
        log.debug("Getting most active users for course {}", courseId);
        
        List<Object[]> users = courseVisitRepository.getMostActiveUsersByCourseId(courseId);
        
        List<Map<String, Object>> results = users.stream()
            .limit(limit)
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", row[0]);
                map.put("visitCount", row[1]);
                return map;
            })
            .toList();
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get statistics for date range
     */
    @GetMapping("/courses/{courseId}/range")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get statistics for date range", 
               description = "Get detailed statistics for a specific date range")
    public ResponseEntity<Map<String, Object>> getStatisticsForDateRange(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @Parameter(description = "Start date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.debug("Getting statistics for course {} from {} to {}", 
                 courseId, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Object[] stats = courseVisitRepository.getStatisticsByCourseIdAndDateRange(
            courseId, startDateTime, endDateTime);
        
        if (stats == null || stats.length == 0) {
            return ResponseEntity.ok(Map.of(
                "courseId", courseId,
                "totalVisits", 0,
                "uniqueVisitors", 0,
                "totalDuration", 0,
                "avgDuration", 0
            ));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("courseId", courseId);
        response.put("startDate", startDate);
        response.put("endDate", endDate);
        response.put("totalVisits", stats[0]);
        response.put("uniqueVisitors", stats[1]);
        response.put("totalDuration", stats[2]);
        response.put("avgDuration", stats[3]);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get engagement metrics
     */
    @GetMapping("/courses/{courseId}/engagement")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get engagement metrics", 
               description = "Get detailed engagement metrics for a course")
    public ResponseEntity<Map<String, Object>> getEngagementMetrics(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        
        log.debug("Getting engagement metrics for course {}", courseId);
        
        String sql = "SELECT " +
                    "COUNT(CASE WHEN visit_type = 'COURSE_VIEW' THEN 1 END) as course_views, " +
                    "COUNT(CASE WHEN visit_type = 'LESSON_VIEW' THEN 1 END) as lesson_views, " +
                    "COUNT(CASE WHEN visit_type = 'QUIZ_VIEW' THEN 1 END) as quiz_views, " +
                    "COUNT(CASE WHEN visit_type = 'QUIZ_START' THEN 1 END) as quiz_starts, " +
                    "COUNT(CASE WHEN visit_type = 'QUIZ_COMPLETE' THEN 1 END) as quiz_completions, " +
                    "COUNT(CASE WHEN visit_type = 'COURSE_ENROLLMENT' THEN 1 END) as enrollments " +
                    "FROM course_visits " +
                    "WHERE course_id = ?";
        
        Map<String, Object> metrics = jdbcTemplate.queryForMap(sql, courseId);
        
        // Calculate completion rate
        Long quizStarts = ((Number) metrics.get("quiz_starts")).longValue();
        Long quizCompletions = ((Number) metrics.get("quiz_completions")).longValue();
        
        Double completionRate = quizStarts > 0 
            ? (quizCompletions.doubleValue() / quizStarts.doubleValue() * 100) 
            : 0.0;
        
        metrics.put("courseId", courseId);
        metrics.put("completionRate", completionRate);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get device statistics
     */
    @GetMapping("/courses/{courseId}/devices")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get device statistics", 
               description = "Get breakdown of visits by device type")
    public ResponseEntity<List<Map<String, Object>>> getDeviceStatistics(
            @Parameter(description = "Course ID") @PathVariable Long courseId) {
        
        log.debug("Getting device statistics for course {}", courseId);
        
        String sql = "SELECT " +
                    "device_type, " +
                    "COUNT(*) as count, " +
                    "ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage " +
                    "FROM course_visits " +
                    "WHERE course_id = ? " +
                    "GROUP BY device_type " +
                    "ORDER BY count DESC";
        
        List<Map<String, Object>> devices = jdbcTemplate.queryForList(sql, courseId);
        
        return ResponseEntity.ok(devices);
    }

    /**
     * Manual creation endpoint for course visits.
     */
    @PostMapping("/visits")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create visit record", description = "Create a course visit record manually (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Visit created successfully",
                content = @Content(schema = @Schema(implementation = CourseVisitResponse.class)))
    })
    public ResponseEntity<CourseVisitResponse> createVisit(@Valid @RequestBody CourseVisitRequest request) {
        var visit = courseVisitService.createVisit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseVisitMapper.toResponse(visit));
    }

    /**
     * Get raw visit log for a course.
     */
    @GetMapping("/courses/{courseId}/visits")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get course visits", description = "Get visit log for a course with optional date range")
    public ResponseEntity<List<CourseVisitResponse>> getCourseVisits(
            @Parameter(description = "Course ID") @PathVariable Long courseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        LocalDateTime[] range = normalizeRange(startDate, endDate);
        var visits = courseVisitService.getCourseVisits(courseId, range[0], range[1]);
        return ResponseEntity.ok(courseVisitMapper.toResponseList(visits));
    }

    /**
     * Get raw visit log for a user.
     */
    @GetMapping("/users/{userId}/visits")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Operation(summary = "Get user visits", description = "Get activity log for a specific user")
    public ResponseEntity<List<CourseVisitResponse>> getUserVisits(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        LocalDateTime[] range = normalizeRange(startDate, endDate);
        var visits = courseVisitService.getUserVisits(userId, range[0], range[1]);
        return ResponseEntity.ok(courseVisitMapper.toResponseList(visits));
    }

    private LocalDateTime[] normalizeRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime normalizedStart = start;
        LocalDateTime normalizedEnd = end;
        if (normalizedStart != null && normalizedEnd == null) {
            normalizedEnd = LocalDateTime.now();
        } else if (normalizedStart == null && normalizedEnd != null) {
            normalizedStart = normalizedEnd.minusDays(30);
        }
        return new LocalDateTime[]{normalizedStart, normalizedEnd};
    }
}





