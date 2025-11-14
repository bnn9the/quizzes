package edu.platform.modules.quiz.repository;

import edu.platform.modules.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    List<QuizAttempt> findByStudentIdAndQuizId(Long studentId, Long quizId);
    
    List<QuizAttempt> findByStudentId(Long studentId);
    
    List<QuizAttempt> findByQuizId(Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.quiz.id = :quizId AND qa.isCompleted = false")
    Optional<QuizAttempt> findActiveAttempt(@Param("studentId") Long studentId, @Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(qa) FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.quiz.id = :quizId")
    Long countByStudentIdAndQuizId(@Param("studentId") Long studentId, @Param("quizId") Long quizId);
    
    @Query("SELECT qa FROM QuizAttempt qa " +
           "JOIN FETCH qa.quiz q " +
           "JOIN FETCH qa.student s " +
           "WHERE qa.id = :id")
    Optional<QuizAttempt> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT qa FROM QuizAttempt qa " +
           "WHERE qa.student.id = :studentId AND qa.quiz.id = :quizId " +
           "ORDER BY qa.attemptNumber DESC")
    List<QuizAttempt> findByStudentIdAndQuizIdOrderByAttemptNumberDesc(@Param("studentId") Long studentId, @Param("quizId") Long quizId);
}

