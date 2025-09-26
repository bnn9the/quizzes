package edu.platform.repository;

import edu.platform.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    List<Quiz> findByCourseId(Long courseId);
    
    List<Quiz> findByCourseIdAndIsActiveTrue(Long courseId);
    
    @Query("SELECT q FROM Quiz q JOIN FETCH q.course WHERE q.id = :id")
    Optional<Quiz> findByIdWithCourse(@Param("id") Long id);
    
    @Query("SELECT q FROM Quiz q JOIN FETCH q.questions WHERE q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);
    
    @Query("SELECT q FROM Quiz q " +
           "JOIN FETCH q.course c " +
           "JOIN FETCH c.teacher " +
           "LEFT JOIN FETCH q.questions qu " +
           "LEFT JOIN FETCH qu.answerOptions " +
           "WHERE q.id = :id")
    Optional<Quiz> findByIdWithFullDetails(@Param("id") Long id);
    
    @Query("SELECT q FROM Quiz q WHERE q.course.teacher.id = :teacherId")
    List<Quiz> findByTeacherId(@Param("teacherId") Long teacherId);
}
