package edu.platform.repository;

import edu.platform.entity.TestResult;
import edu.platform.entity.enums.TestResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    Optional<TestResult> findByQuizAttemptId(Long quizAttemptId);
    
    List<TestResult> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    List<TestResult> findByQuizIdOrderByCreatedAtDesc(Long quizId);
    
    List<TestResult> findByStudentIdAndQuizId(Long studentId, Long quizId);
    
    List<TestResult> findByStatus(TestResultStatus status);
    
    @Query("SELECT tr FROM TestResult tr " +
           "JOIN FETCH tr.student " +
           "JOIN FETCH tr.quiz " +
           "WHERE tr.id = :id")
    Optional<TestResult> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT tr FROM TestResult tr " +
           "WHERE tr.student.id = :studentId " +
           "AND tr.status = :status " +
           "ORDER BY tr.createdAt DESC")
    List<TestResult> findByStudentIdAndStatus(@Param("studentId") Long studentId, 
                                               @Param("status") TestResultStatus status);
    
    @Query("SELECT tr FROM TestResult tr " +
           "WHERE tr.quiz.id = :quizId " +
           "AND tr.status = 'PASSED' " +
           "ORDER BY tr.score DESC")
    List<TestResult> findTopScoresByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT AVG(tr.percentage) FROM TestResult tr " +
           "WHERE tr.quiz.id = :quizId " +
           "AND tr.status IN ('PASSED', 'FAILED')")
    Double getAveragePercentageByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(tr) FROM TestResult tr " +
           "WHERE tr.quiz.id = :quizId " +
           "AND tr.status = 'PASSED'")
    Long countPassedByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(tr) FROM TestResult tr " +
           "WHERE tr.quiz.id = :quizId " +
           "AND tr.status = 'FAILED'")
    Long countFailedByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT tr FROM TestResult tr " +
           "WHERE tr.completedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY tr.completedAt DESC")
    List<TestResult> findCompletedBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT tr FROM TestResult tr " +
           "WHERE tr.status = 'IN_PROGRESS' " +
           "AND tr.createdAt < :timeout")
    List<TestResult> findTimedOutAttempts(@Param("timeout") LocalDateTime timeout);
    
    boolean existsByQuizAttemptId(Long quizAttemptId);
}