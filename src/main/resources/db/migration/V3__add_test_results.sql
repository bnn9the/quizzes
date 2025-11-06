CREATE TABLE test_results (
    id BIGSERIAL PRIMARY KEY,
    quiz_attempt_id BIGINT NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    
    -- Score information (increased precision to support large point values)
    score DECIMAL(10,2) NOT NULL,
    max_score DECIMAL(10,2) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    passing_score DECIMAL(5,2),
    
    -- Status tracking
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    
    -- Time tracking
    time_spent_seconds BIGINT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    
    -- Statistics
    correct_answers INTEGER,
    total_questions INTEGER,
    calculation_time_ms BIGINT,
    
    -- Error handling
    error_message VARCHAR(500),
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_test_results_quiz_attempt 
        FOREIGN KEY (quiz_attempt_id) 
        REFERENCES quiz_attempts(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_test_results_student 
        FOREIGN KEY (student_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_test_results_quiz 
        FOREIGN KEY (quiz_id) 
        REFERENCES quizzes(id) 
        ON DELETE CASCADE,
    
    -- Data validation constraints
    CONSTRAINT check_percentage_range 
        CHECK (percentage >= 0 AND percentage <= 100),
    
    CONSTRAINT check_passing_score_range 
        CHECK (passing_score IS NULL OR (passing_score >= 0 AND passing_score <= 100)),
    
    CONSTRAINT check_scores_positive 
        CHECK (score >= 0 AND max_score >= 0),
    
    CONSTRAINT check_time_positive 
        CHECK (time_spent_seconds IS NULL OR time_spent_seconds >= 0)
);

-- Create indexes for better query performance
CREATE INDEX idx_test_results_quiz_attempt ON test_results(quiz_attempt_id);
CREATE INDEX idx_test_results_student ON test_results(student_id);
CREATE INDEX idx_test_results_quiz ON test_results(quiz_id);
CREATE INDEX idx_test_results_status ON test_results(status);
CREATE INDEX idx_test_results_completed_at ON test_results(completed_at);

-- Composite indexes for common queries
CREATE INDEX idx_test_results_quiz_student ON test_results(quiz_id, student_id);
CREATE INDEX idx_test_results_status_completed ON test_results(status, completed_at);

-- Add table and column comments for documentation
COMMENT ON TABLE test_results IS 'Stores test completion results with scores, time spent, and status. Protected by Circuit Breaker pattern.';
COMMENT ON COLUMN test_results.quiz_attempt_id IS 'Unique reference to quiz attempt (one-to-one relationship)';
COMMENT ON COLUMN test_results.student_id IS 'Reference to student who took the test';
COMMENT ON COLUMN test_results.quiz_id IS 'Reference to the quiz';
COMMENT ON COLUMN test_results.score IS 'Student achieved score (max 99,999,999.99)';
COMMENT ON COLUMN test_results.max_score IS 'Maximum possible score (max 99,999,999.99)';
COMMENT ON COLUMN test_results.percentage IS 'Score percentage: 0.00 to 100.00';
COMMENT ON COLUMN test_results.passing_score IS 'Required percentage to pass: 0.00 to 100.00 (e.g., 70.00 for 70%)';
COMMENT ON COLUMN test_results.status IS 'Test status: PASSED, FAILED, IN_PROGRESS, TIMEOUT, ERROR';
COMMENT ON COLUMN test_results.time_spent_seconds IS 'Time spent on test in seconds';
COMMENT ON COLUMN test_results.started_at IS 'Test start timestamp';
COMMENT ON COLUMN test_results.completed_at IS 'Test completion timestamp';
COMMENT ON COLUMN test_results.correct_answers IS 'Number of correctly answered questions';
COMMENT ON COLUMN test_results.total_questions IS 'Total number of questions in the quiz';
COMMENT ON COLUMN test_results.calculation_time_ms IS 'Time taken to calculate result in milliseconds';
COMMENT ON COLUMN test_results.error_message IS 'Error message if calculation failed (max 500 chars)';