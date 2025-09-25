package edu.platform.mapper;

import edu.platform.dto.response.QuizAttemptResponse;
import edu.platform.entity.QuizAttempt;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {QuizMapper.class, UserMapper.class})
public interface QuizAttemptMapper {
    
    QuizAttemptResponse toResponse(QuizAttempt quizAttempt);
    
    List<QuizAttemptResponse> toResponseList(List<QuizAttempt> quizAttempts);
}
