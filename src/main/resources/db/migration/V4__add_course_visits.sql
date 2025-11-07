CREATE TABLE course_visits (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT,
    quiz_id BIGINT,
    
    -- Visit metadata
    visit_type VARCHAR(20) NOT NULL DEFAULT 'COURSE_VIEW',
    duration_seconds INTEGER,
    device_type VARCHAR(50),
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    
    -- Timestamps
    visited_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys
    CONSTRAINT fk_course_visits_user 
        FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_course_visits_course 
        FOREIGN KEY (course_id) 
        REFERENCES courses(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_course_visits_lesson 
        FOREIGN KEY (lesson_id) 
        REFERENCES lessons(id) 
        ON DELETE SET NULL,
    
    CONSTRAINT fk_course_visits_quiz 
        FOREIGN KEY (quiz_id) 
        REFERENCES quizzes(id) 
        ON DELETE SET NULL,
    
    -- Constraints
    CONSTRAINT check_visit_duration 
        CHECK (duration_seconds IS NULL OR duration_seconds >= 0)
);

-- Indexes for performance
CREATE INDEX idx_course_visits_user ON course_visits(user_id);
CREATE INDEX idx_course_visits_course ON course_visits(course_id);
CREATE INDEX idx_course_visits_lesson ON course_visits(lesson_id);
CREATE INDEX idx_course_visits_quiz ON course_visits(quiz_id);
CREATE INDEX idx_course_visits_visited_at ON course_visits(visited_at DESC);
CREATE INDEX idx_course_visits_type ON course_visits(visit_type);

-- Composite indexes for common queries
CREATE INDEX idx_course_visits_user_course ON course_visits(user_id, course_id);
CREATE INDEX idx_course_visits_course_date ON course_visits(course_id, visited_at DESC);
CREATE INDEX idx_course_visits_user_date ON course_visits(user_id, visited_at DESC);

-- Aggregated statistics table
CREATE TABLE course_statistics_aggregated (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL,
    
    -- Period (daily, weekly, monthly)
    period_type VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Metrics
    total_visits BIGINT NOT NULL DEFAULT 0,
    unique_visitors BIGINT NOT NULL DEFAULT 0,
    total_duration_seconds BIGINT NOT NULL DEFAULT 0,
    avg_duration_seconds INTEGER NOT NULL DEFAULT 0,
    
    -- Engagement metrics
    lesson_views BIGINT NOT NULL DEFAULT 0,
    quiz_attempts BIGINT NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,2) DEFAULT 0.00,
    
    -- Metadata
    last_aggregated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key
    CONSTRAINT fk_stats_course 
        FOREIGN KEY (course_id) 
        REFERENCES courses(id) 
        ON DELETE CASCADE,
    
    -- Unique constraint: one record per course per period
    CONSTRAINT unique_course_period 
        UNIQUE (course_id, period_type, period_start),
    
    -- Constraints
    CONSTRAINT check_period_dates 
        CHECK (period_end >= period_start),
    CONSTRAINT check_metrics_positive 
        CHECK (total_visits >= 0 AND unique_visitors >= 0 AND total_duration_seconds >= 0)
);

-- Indexes for aggregated statistics
CREATE INDEX idx_stats_course ON course_statistics_aggregated(course_id);
CREATE INDEX idx_stats_period ON course_statistics_aggregated(period_type, period_start);
CREATE INDEX idx_stats_course_period ON course_statistics_aggregated(course_id, period_type, period_start DESC);

-- Comments
COMMENT ON TABLE course_visits IS 'Tracks user visits to courses, lessons, and quizzes for activity analysis';
COMMENT ON COLUMN course_visits.visit_type IS 'Type of visit: COURSE_VIEW, LESSON_VIEW, QUIZ_VIEW, QUIZ_START, QUIZ_COMPLETE';
COMMENT ON COLUMN course_visits.duration_seconds IS 'Time spent on the page in seconds';
COMMENT ON COLUMN course_visits.device_type IS 'Device type: DESKTOP, MOBILE, TABLET';

COMMENT ON TABLE course_statistics_aggregated IS 'Pre-aggregated course statistics for performance';
COMMENT ON COLUMN course_statistics_aggregated.period_type IS 'Period type: DAILY, WEEKLY, MONTHLY';
COMMENT ON COLUMN course_statistics_aggregated.completion_rate IS 'Percentage of users who completed the course';