package edu.platform.repository;

import edu.platform.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    
    List<AnswerOption> findByQuestionIdOrderByOrderIndexAsc(Long questionId);
    
    @Query("SELECT ao FROM AnswerOption ao WHERE ao.question.id = :questionId AND ao.isCorrect = true")
    List<AnswerOption> findCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
    
    @Query("SELECT COUNT(ao) FROM AnswerOption ao WHERE ao.question.id = :questionId AND ao.isCorrect = true")
    Long countCorrectAnswersByQuestionId(@Param("questionId") Long questionId);
}
