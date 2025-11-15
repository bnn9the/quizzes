package edu.platform.modules.course.repository;

import edu.platform.modules.course.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.id = :courseId ORDER BY l.orderIndex ASC")
    List<Lesson> findByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l JOIN FETCH l.course WHERE l.id = :id")
    Optional<Lesson> findByIdWithCourse(@Param("id") Long id);
    
    @Query("SELECT l FROM Lesson l " +
           "LEFT JOIN FETCH l.mediaAssets " +
           "WHERE l.id = :id")
    Optional<Lesson> findByIdWithMediaAssets(@Param("id") Long id);
    
    @Query("SELECT l FROM Lesson l " +
           "JOIN FETCH l.course c " +
           "LEFT JOIN FETCH l.mediaAssets " +
           "WHERE l.id = :id")
    Optional<Lesson> findByIdWithFullDetails(@Param("id") Long id);
    
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT l FROM Lesson l WHERE l.id = :id")
    Optional<Lesson> findByIdWithOptimisticLock(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lesson l WHERE l.id = :id")
    Optional<Lesson> findByIdWithPessimisticLock(@Param("id") Long id);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT MAX(l.orderIndex) FROM Lesson l WHERE l.course.id = :courseId")
    Integer getMaxOrderIndexByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.course.teacher.id = :teacherId ORDER BY l.createdAt DESC")
    List<Lesson> findByTeacherId(@Param("teacherId") Long teacherId);
}
