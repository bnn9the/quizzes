package edu.platform.modules.quiz.mapper;

import edu.platform.modules.user.mapper.UserMapper;
import edu.platform.modules.quiz.dto.response.QuizAttemptResponse;
import edu.platform.modules.quiz.entity.QuizAttempt;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuizMapper.class, UserMapper.class})
public interface QuizAttemptMapper {
    
    QuizAttemptResponse toResponse(QuizAttempt quizAttempt);
    
    List<QuizAttemptResponse> toResponseList(List<QuizAttempt> quizAttempts);
}





