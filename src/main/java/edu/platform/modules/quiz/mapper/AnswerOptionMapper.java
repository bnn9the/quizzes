package edu.platform.modules.quiz.mapper;

import edu.platform.modules.quiz.dto.request.AnswerOptionRequest;
import edu.platform.modules.quiz.dto.response.AnswerOptionResponse;
import edu.platform.modules.quiz.entity.AnswerOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerOptionMapper {
    
    AnswerOptionResponse toResponse(AnswerOption answerOption);
    
    List<AnswerOptionResponse> toResponseList(List<AnswerOption> answerOptions);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    AnswerOption toEntity(AnswerOptionRequest request);
}


