package edu.platform.modules.quiz.mapper;

import edu.platform.modules.quiz.dto.request.QuestionRequest;
import edu.platform.modules.quiz.dto.response.QuestionResponse;
import edu.platform.modules.quiz.entity.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AnswerOptionMapper.class})
public interface QuestionMapper {
    
    QuestionResponse toResponse(Question question);
    
    List<QuestionResponse> toResponseList(List<Question> questions);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "quiz", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "answerOptions", ignore = true)
    @Mapping(target = "studentAnswers", ignore = true)
    Question toEntity(QuestionRequest request);
}


