package edu.platform.modules.analytics.scheduler;

import edu.platform.modules.analytics.service.CourseStatisticsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Scheduled task for periodic course statistics aggregation.
 * Runs daily to aggregate statistics for better query performance.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsAggregationScheduler {
    
    private final CourseStatisticsAggregationService aggregationService;
    
    /**
     * Aggregate daily statistics every day at 2:00 AM.
     * Configurable via: statistics.aggregation.daily-cron
     */
    @Scheduled(cron = "${statistics.aggregation.daily-cron:0 0 2 * * *}")
    public void aggregateDailyStatistics() {
        log.info("Starting scheduled daily statistics aggregation");
        
        try {
            CompletableFuture<Integer> result = aggregationService.aggregateDailyStatistics();
            
            result.thenAccept(count -> 
                log.info("Daily statistics aggregation completed: {} courses processed", count)
            ).exceptionally(throwable -> {
                log.error("Daily statistics aggregation failed", throwable);
                return null;
            });
            
        } catch (Exception e) {
            log.error("Error during daily statistics aggregation", e);
        }
    }
    
    /**
     * Aggregate weekly statistics every Monday at 3:00 AM.
     * Configurable via: statistics.aggregation.weekly-cron
     */
    @Scheduled(cron = "${statistics.aggregation.weekly-cron:0 0 3 * * MON}")
    public void aggregateWeeklyStatistics() {
        log.info("Starting scheduled weekly statistics aggregation");
        
        try {
            LocalDate weekStart = LocalDate.now().minusWeeks(1);
            // Get Monday of last week
            weekStart = weekStart.minusDays(weekStart.getDayOfWeek().getValue() - 1);
            
            // Note: This would need to be implemented to iterate over all courses
            // For now, logging the intent
            log.info("Weekly aggregation would process week starting: {}", weekStart);
            
        } catch (Exception e) {
            log.error("Error during weekly statistics aggregation", e);
        }
    }
    
    /**
     * Aggregate monthly statistics on the 1st of each month at 4:00 AM.
     * Configurable via: statistics.aggregation.monthly-cron
     */
    @Scheduled(cron = "${statistics.aggregation.monthly-cron:0 0 4 1 * *}")
    public void aggregateMonthlyStatistics() {
        log.info("Starting scheduled monthly statistics aggregation");
        
        try {
            LocalDate monthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
            
            // Note: This would need to be implemented to iterate over all courses
            // For now, logging the intent
            log.info("Monthly aggregation would process month starting: {}", monthStart);
            
        } catch (Exception e) {
            log.error("Error during monthly statistics aggregation", e);
        }
    }
    
    /**
     * Clean up old visit records every week on Sunday at 1:00 AM.
     * Keeps only last 90 days of raw visit data.
     * Configurable via: statistics.aggregation.cleanup-cron
     */
    @Scheduled(cron = "${statistics.aggregation.cleanup-cron:0 0 1 * * SUN}")
    public void cleanupOldVisits() {
        log.info("Starting cleanup of old visit records");
        
        try {
            // Cleanup logic would go here
            // For now, just logging
            log.info("Old visit cleanup would execute here");
            
        } catch (Exception e) {
            log.error("Error during visit cleanup", e);
        }
    }
}
