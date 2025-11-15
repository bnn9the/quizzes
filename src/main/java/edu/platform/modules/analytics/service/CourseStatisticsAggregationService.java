package edu.platform.modules.analytics.service;

import edu.platform.modules.course.entity.Course;
import edu.platform.modules.course.repository.CourseRepository;
import edu.platform.modules.analytics.repository.CourseVisitRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for aggregating course statistics.
 * Uses Bulkhead pattern to limit concurrent aggregations and prevent resource exhaustion.
 * Uses Retry pattern for resilient database operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseStatisticsAggregationService {
    
    private final CourseVisitRepository courseVisitRepository;
    private final CourseRepository courseRepository;
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * Aggregate statistics for all courses (daily).
     * Protected by Bulkhead to limit concurrent executions.
     * Protected by Retry for database resilience.
     */
    @Bulkhead(name = "statisticsAggregation", fallbackMethod = "aggregateDailyStatisticsFallback")
    @Retry(name = "statisticsAggregation")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public CompletableFuture<Integer> aggregateDailyStatistics() {
        log.info("Starting daily statistics aggregation for all courses");
        long startTime = System.currentTimeMillis();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        List<Course> courses = courseRepository.findAll();
        int aggregatedCount = 0;
        
        for (Course course : courses) {
            try {
                aggregateDailyStatisticsForCourse(course.getId(), yesterday);
                aggregatedCount++;
            } catch (Exception e) {
                log.error("Failed to aggregate statistics for course: {}", course.getId(), e);
                // Continue with other courses
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Completed daily statistics aggregation: {} courses processed in {}ms", 
                aggregatedCount, duration);
        
        return CompletableFuture.completedFuture(aggregatedCount);
    }
    
    /**
     * Aggregate statistics for a specific course and date.
     * Uses batch operations for performance.
     */
    @Retry(name = "statisticsAggregation")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void aggregateDailyStatisticsForCourse(Long courseId, LocalDate date) {
        log.debug("Aggregating daily statistics for course: {} on {}", courseId, date);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        // Get raw statistics from course_visits
        Object[] stats = courseVisitRepository.getStatisticsByCourseIdAndDateRange(
            courseId, startOfDay, endOfDay);
        
        if (stats == null || stats.length == 0) {
            log.debug("No visits found for course {} on {}", courseId, date);
            return;
        }
        
        Long totalVisits = ((Number) stats[0]).longValue();
        Long uniqueVisitors = ((Number) stats[1]).longValue();
        Long totalDuration = ((Number) stats[2]).longValue();
        Double avgDuration = ((Number) stats[3]).doubleValue();
        
        // Count lesson and quiz activity
        Long lessonViews = countVisitsByType(courseId, startOfDay, endOfDay, "LESSON_VIEW");
        Long quizAttempts = countVisitsByType(courseId, startOfDay, endOfDay, "QUIZ_START");
        
        // Calculate completion rate
        Double completionRate = calculateCompletionRate(courseId, startOfDay, endOfDay);
        
        // Upsert aggregated statistics
        upsertDailyStatistics(courseId, date, totalVisits, uniqueVisitors, totalDuration,
                            avgDuration.intValue(), lessonViews, quizAttempts, completionRate);
        
        log.debug("Aggregated statistics for course {}: visits={}, unique={}, duration={}s", 
                 courseId, totalVisits, uniqueVisitors, totalDuration);
    }
    
    /**
     * Aggregate weekly statistics for a course
     */
    @Retry(name = "statisticsAggregation")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void aggregateWeeklyStatistics(Long courseId, LocalDate weekStart) {
        log.debug("Aggregating weekly statistics for course: {} starting {}", courseId, weekStart);
        
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);
        
        Object[] stats = courseVisitRepository.getStatisticsByCourseIdAndDateRange(
            courseId, startDateTime, endDateTime);
        
        if (stats == null || stats.length == 0) {
            return;
        }
        
        Long totalVisits = ((Number) stats[0]).longValue();
        Long uniqueVisitors = ((Number) stats[1]).longValue();
        Long totalDuration = ((Number) stats[2]).longValue();
        Double avgDuration = ((Number) stats[3]).doubleValue();
        
        Long lessonViews = countVisitsByType(courseId, startDateTime, endDateTime, "LESSON_VIEW");
        Long quizAttempts = countVisitsByType(courseId, startDateTime, endDateTime, "QUIZ_START");
        Double completionRate = calculateCompletionRate(courseId, startDateTime, endDateTime);
        
        upsertWeeklyStatistics(courseId, weekStart, weekEnd, totalVisits, uniqueVisitors,
                             totalDuration, avgDuration.intValue(), lessonViews, quizAttempts, 
                             completionRate);
    }
    
    /**
     * Aggregate monthly statistics for a course
     */
    @Retry(name = "statisticsAggregation")
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void aggregateMonthlyStatistics(Long courseId, LocalDate monthStart) {
        log.debug("Aggregating monthly statistics for course: {} for {}", courseId, monthStart);
        
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.atTime(LocalTime.MAX);
        
        Object[] stats = courseVisitRepository.getStatisticsByCourseIdAndDateRange(
            courseId, startDateTime, endDateTime);
        
        if (stats == null || stats.length == 0) {
            return;
        }
        
        Long totalVisits = ((Number) stats[0]).longValue();
        Long uniqueVisitors = ((Number) stats[1]).longValue();
        Long totalDuration = ((Number) stats[2]).longValue();
        Double avgDuration = ((Number) stats[3]).doubleValue();
        
        Long lessonViews = countVisitsByType(courseId, startDateTime, endDateTime, "LESSON_VIEW");
        Long quizAttempts = countVisitsByType(courseId, startDateTime, endDateTime, "QUIZ_START");
        Double completionRate = calculateCompletionRate(courseId, startDateTime, endDateTime);
        
        upsertMonthlyStatistics(courseId, monthStart, monthEnd, totalVisits, uniqueVisitors,
                              totalDuration, avgDuration.intValue(), lessonViews, quizAttempts, 
                              completionRate);
    }
    
    /**
     * Count visits by type in date range
     */
    private Long countVisitsByType(Long courseId, LocalDateTime start, LocalDateTime end, 
                                   String visitType) {
        String sql = "SELECT COUNT(*) FROM course_visits " +
                    "WHERE course_id = ? AND visit_type = ? " +
                    "AND visited_at BETWEEN ? AND ?";
        
        return jdbcTemplate.queryForObject(sql, Long.class, courseId, visitType, start, end);
    }
    
    /**
     * Calculate completion rate for course in date range
     */
    private Double calculateCompletionRate(Long courseId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT " +
                    "CASE WHEN COUNT(DISTINCT cv.user_id) = 0 THEN 0 " +
                    "ELSE (COUNT(DISTINCT CASE WHEN cv.visit_type = 'QUIZ_COMPLETE' " +
                    "THEN cv.user_id END)::DECIMAL / COUNT(DISTINCT cv.user_id) * 100) END " +
                    "FROM course_visits cv " +
                    "WHERE cv.course_id = ? AND cv.visited_at BETWEEN ? AND ?";
        
        Double rate = jdbcTemplate.queryForObject(sql, Double.class, courseId, start, end);
        return rate != null ? rate : 0.0;
    }
    
    /**
     * Upsert daily statistics
     */
    private void upsertDailyStatistics(Long courseId, LocalDate date, Long totalVisits,
                                      Long uniqueVisitors, Long totalDuration, Integer avgDuration,
                                      Long lessonViews, Long quizAttempts, Double completionRate) {
        String sql = "INSERT INTO course_statistics_aggregated " +
                    "(course_id, period_type, period_start, period_end, total_visits, " +
                    "unique_visitors, total_duration_seconds, avg_duration_seconds, " +
                    "lesson_views, quiz_attempts, completion_rate, last_aggregated_at) " +
                    "VALUES (?, 'DAILY', ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (course_id, period_type, period_start) " +
                    "DO UPDATE SET " +
                    "total_visits = EXCLUDED.total_visits, " +
                    "unique_visitors = EXCLUDED.unique_visitors, " +
                    "total_duration_seconds = EXCLUDED.total_duration_seconds, " +
                    "avg_duration_seconds = EXCLUDED.avg_duration_seconds, " +
                    "lesson_views = EXCLUDED.lesson_views, " +
                    "quiz_attempts = EXCLUDED.quiz_attempts, " +
                    "completion_rate = EXCLUDED.completion_rate, " +
                    "last_aggregated_at = NOW(), " +
                    "updated_at = NOW()";
        
        jdbcTemplate.update(sql, courseId, date, date, totalVisits, uniqueVisitors,
                          totalDuration, avgDuration, lessonViews, quizAttempts, completionRate);
    }
    
    /**
     * Upsert weekly statistics
     */
    private void upsertWeeklyStatistics(Long courseId, LocalDate weekStart, LocalDate weekEnd,
                                       Long totalVisits, Long uniqueVisitors, Long totalDuration,
                                       Integer avgDuration, Long lessonViews, Long quizAttempts,
                                       Double completionRate) {
        String sql = "INSERT INTO course_statistics_aggregated " +
                    "(course_id, period_type, period_start, period_end, total_visits, " +
                    "unique_visitors, total_duration_seconds, avg_duration_seconds, " +
                    "lesson_views, quiz_attempts, completion_rate, last_aggregated_at) " +
                    "VALUES (?, 'WEEKLY', ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (course_id, period_type, period_start) " +
                    "DO UPDATE SET " +
                    "period_end = EXCLUDED.period_end, " +
                    "total_visits = EXCLUDED.total_visits, " +
                    "unique_visitors = EXCLUDED.unique_visitors, " +
                    "total_duration_seconds = EXCLUDED.total_duration_seconds, " +
                    "avg_duration_seconds = EXCLUDED.avg_duration_seconds, " +
                    "lesson_views = EXCLUDED.lesson_views, " +
                    "quiz_attempts = EXCLUDED.quiz_attempts, " +
                    "completion_rate = EXCLUDED.completion_rate, " +
                    "last_aggregated_at = NOW(), " +
                    "updated_at = NOW()";
        
        jdbcTemplate.update(sql, courseId, weekStart, weekEnd, totalVisits, uniqueVisitors,
                          totalDuration, avgDuration, lessonViews, quizAttempts, completionRate);
    }
    
    /**
     * Upsert monthly statistics
     */
    private void upsertMonthlyStatistics(Long courseId, LocalDate monthStart, LocalDate monthEnd,
                                        Long totalVisits, Long uniqueVisitors, Long totalDuration,
                                        Integer avgDuration, Long lessonViews, Long quizAttempts,
                                        Double completionRate) {
        String sql = "INSERT INTO course_statistics_aggregated " +
                    "(course_id, period_type, period_start, period_end, total_visits, " +
                    "unique_visitors, total_duration_seconds, avg_duration_seconds, " +
                    "lesson_views, quiz_attempts, completion_rate, last_aggregated_at) " +
                    "VALUES (?, 'MONTHLY', ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                    "ON CONFLICT (course_id, period_type, period_start) " +
                    "DO UPDATE SET " +
                    "period_end = EXCLUDED.period_end, " +
                    "total_visits = EXCLUDED.total_visits, " +
                    "unique_visitors = EXCLUDED.unique_visitors, " +
                    "total_duration_seconds = EXCLUDED.total_duration_seconds, " +
                    "avg_duration_seconds = EXCLUDED.avg_duration_seconds, " +
                    "lesson_views = EXCLUDED.lesson_views, " +
                    "quiz_attempts = EXCLUDED.quiz_attempts, " +
                    "completion_rate = EXCLUDED.completion_rate, " +
                    "last_aggregated_at = NOW(), " +
                    "updated_at = NOW()";
        
        jdbcTemplate.update(sql, courseId, monthStart, monthEnd, totalVisits, uniqueVisitors,
                          totalDuration, avgDuration, lessonViews, quizAttempts, completionRate);
    }
    
    /**
     * Fallback method when aggregation is rejected by bulkhead
     */
    private CompletableFuture<Integer> aggregateDailyStatisticsFallback(Exception e) {
        log.warn("Statistics aggregation rejected by bulkhead or failed: {}", e.getMessage());
        return CompletableFuture.completedFuture(0);
    }
}

