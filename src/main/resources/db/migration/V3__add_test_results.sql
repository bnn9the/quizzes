CREATE TABLE test_results (
    id BIGSERIAL PRIMARY KEY,
    quiz_attempt_id BIGINT NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    quiz_id BIGINT NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    max_score DECIMAL(5,2) NOT NULL,
    percentage DECIMAL(5,2) NOT NULL,
    passing_score DECIMAL(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    time_spent_seconds BIGINT,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    correct_answers INTEGER,
    total_questions INTEGER,
    calculation_time_ms BIGINT,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (quiz_attempt_id) REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_test_results_quiz_attempt ON test_results(quiz_attempt_id);
CREATE INDEX idx_test_results_student ON test_results(student_id);
CREATE INDEX idx_test_results_quiz ON test_results(quiz_id);
CREATE INDEX idx_test_results_status ON test_results(status);
CREATE INDEX idx_test_results_completed_at ON test_results(completed_at);

-- Add comment for documentation
COMMENT ON TABLE test_results IS 'Stores test completion results with scores, time spent, and status';
COMMENT ON COLUMN test_results.status IS 'Status: PASSED, FAILED, IN_PROGRESS, TIMEOUT, ERROR';
COMMENT ON COLUMN test_results.time_spent_seconds IS 'Time spent on test in seconds';
COMMENT ON COLUMN test_results.calculation_time_ms IS 'Time taken to calculate result in milliseconds';
COMMENT ON COLUMN test_results.passing_score IS 'Minimum percentage required to pass (e.g., 70.00 for 70%)';