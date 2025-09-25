package edu.platform.mapper;

import edu.platform.dto.request.AnswerOptionRequest;
import edu.platform.dto.response.AnswerOptionResponse;
import edu.platform.entity.AnswerOption;
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
