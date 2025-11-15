package edu.platform.modules.quiz.repository;

import edu.platform.modules.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    
    List<Question> findByQuizIdOrderByOrderIndexAsc(Long quizId);
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.answerOptions WHERE q.quiz.id = :quizId ORDER BY q.orderIndex ASC")
    List<Question> findByQuizIdWithAnswerOptions(@Param("quizId") Long quizId);
    
    @Query("SELECT COUNT(q) FROM Question q WHERE q.quiz.id = :quizId")
    Long countByQuizId(@Param("quizId") Long quizId);
    
    @Query("SELECT SUM(q.points) FROM Question q WHERE q.quiz.id = :quizId")
    Integer getTotalPointsByQuizId(@Param("quizId") Long quizId);
}

