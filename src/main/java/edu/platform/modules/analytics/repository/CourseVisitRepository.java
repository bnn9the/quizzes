package edu.platform.modules.analytics.repository;

import edu.platform.modules.analytics.entity.CourseVisit;
import edu.platform.modules.analytics.enums.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CourseVisitRepository extends JpaRepository<CourseVisit, Long> {
    
    /**
     * Find visits by user
     */
    List<CourseVisit> findByUserIdOrderByVisitedAtDesc(Long userId);
    
    /**
     * Find visits by course
     */
    List<CourseVisit> findByCourseIdOrderByVisitedAtDesc(Long courseId);
    
    /**
     * Find visits by user and course
     */
    List<CourseVisit> findByUserIdAndCourseIdOrderByVisitedAtDesc(Long userId, Long courseId);
    
    @Query("SELECT cv FROM CourseVisit cv " +
           "WHERE cv.user.id = :userId " +
           "AND cv.visitedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cv.visitedAt DESC")
    List<CourseVisit> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find visits in date range
     */
    @Query("SELECT cv FROM CourseVisit cv " +
           "WHERE cv.visitedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cv.visitedAt DESC")
    List<CourseVisit> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find visits by course in date range
     */
    @Query("SELECT cv FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId " +
           "AND cv.visitedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cv.visitedAt DESC")
    List<CourseVisit> findByCourseIdAndDateRange(@Param("courseId") Long courseId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count total visits for course
     */
    @Query("SELECT COUNT(cv) FROM CourseVisit cv WHERE cv.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Count unique visitors for course
     */
    @Query("SELECT COUNT(DISTINCT cv.user.id) FROM CourseVisit cv WHERE cv.course.id = :courseId")
    Long countUniqueVisitorsByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Count visits by type for course
     */
    @Query("SELECT COUNT(cv) FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId AND cv.visitType = :visitType")
    Long countByCourseIdAndVisitType(@Param("courseId") Long courseId, 
                                      @Param("visitType") VisitType visitType);
    
    /**
     * Get total duration for course
     */
    @Query("SELECT COALESCE(SUM(cv.durationSeconds), 0) FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId")
    Long getTotalDurationByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Get average duration for course
     */
    @Query("SELECT COALESCE(AVG(cv.durationSeconds), 0) FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId AND cv.durationSeconds IS NOT NULL")
    Double getAverageDurationByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Get visit statistics for course in date range
     */
    @Query("SELECT " +
           "COUNT(cv) as totalVisits, " +
           "COUNT(DISTINCT cv.user.id) as uniqueVisitors, " +
           "COALESCE(SUM(cv.durationSeconds), 0) as totalDuration, " +
           "COALESCE(AVG(cv.durationSeconds), 0) as avgDuration " +
           "FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId " +
           "AND cv.visitedAt BETWEEN :startDate AND :endDate")
    Object[] getStatisticsByCourseIdAndDateRange(@Param("courseId") Long courseId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * Get most active users for course
     */
    @Query("SELECT cv.user.id, COUNT(cv) as visitCount " +
           "FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId " +
           "GROUP BY cv.user.id " +
           "ORDER BY visitCount DESC")
    List<Object[]> getMostActiveUsersByCourseId(@Param("courseId") Long courseId);
    
    /**
     * Get visit trend (daily counts) for course
     */
    @Query("SELECT DATE(cv.visitedAt), COUNT(cv) " +
           "FROM CourseVisit cv " +
           "WHERE cv.course.id = :courseId " +
           "AND cv.visitedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(cv.visitedAt) " +
           "ORDER BY DATE(cv.visitedAt)")
    List<Object[]> getVisitTrendByCourseId(@Param("courseId") Long courseId,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Delete old visits (for data retention)
     */
    @Modifying
    @Query("DELETE FROM CourseVisit cv WHERE cv.visitedAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}


