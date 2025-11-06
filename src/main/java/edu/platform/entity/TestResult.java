package edu.platform.entity;

import edu.platform.entity.enums.TestResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_results", indexes = {
    @Index(name = "idx_test_results_quiz_attempt", columnList = "quiz_attempt_id"),
    @Index(name = "idx_test_results_student", columnList = "student_id"),
    @Index(name = "idx_test_results_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_attempt_id", nullable = false, unique = true)
    private QuizAttempt quizAttempt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    // ИСПРАВЛЕНО: precision = 10 вместо 5 (позволяет значения до 99,999,999.99)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal score;
    
    @Column(name = "max_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxScore;
    
    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;
    
    @Column(name = "passing_score", precision = 5, scale = 2)
    private BigDecimal passingScore;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TestResultStatus status = TestResultStatus.IN_PROGRESS;
    
    @Column(name = "time_spent_seconds")
    private Long timeSpentSeconds;
    
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "correct_answers")
    private Integer correctAnswers;
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "calculation_time_ms")
    private Long calculationTimeMs;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Calculate time spent in human-readable format
     */
    @Transient
    public String getFormattedTimeSpent() {
        if (timeSpentSeconds == null) {
            return "N/A";
        }
        Duration duration = Duration.ofSeconds(timeSpentSeconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Check if test passed based on passing score
     */
    @Transient
    public boolean isPassed() {
        if (status != TestResultStatus.PASSED && status != TestResultStatus.FAILED) {
            return false;
        }
        if (passingScore == null) {
            return status == TestResultStatus.PASSED;
        }
        return percentage.compareTo(passingScore) >= 0;
    }
}