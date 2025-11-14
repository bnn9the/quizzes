package edu.platform.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring Scheduling for background tasks.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Spring will automatically detect @Scheduled methods
}