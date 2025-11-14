package edu.platform.modules.quiz.scheduler;

import edu.platform.modules.quiz.service.TestResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to process timed out test attempts.
 * Runs periodically to mark old IN_PROGRESS attempts as TIMEOUT.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestResultCleanupScheduler {
    
    private final TestResultService testResultService;
    
    @Value("${test-result.processing.timeout-minutes:30}")
    private int timeoutMinutes;
    
    /**
     * Process timed out test attempts every 15 minutes.
     * Configurable via: test-result.processing.cleanup-cron
     */
    @Scheduled(cron = "${test-result.processing.cleanup-cron:0 */15 * * * *}")
    public void processTimedOutAttempts() {
        log.info("Running scheduled cleanup for timed out test attempts");
        
        try {
            testResultService.processTimedOutAttempts(timeoutMinutes);
        } catch (Exception e) {
            log.error("Error processing timed out attempts", e);
        }
    }
}
