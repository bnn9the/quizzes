package edu.platform.modules.quiz.mapper;

import edu.platform.modules.course.mapper.CourseMapper;
import edu.platform.modules.quiz.dto.request.QuizRequest;
import edu.platform.modules.quiz.dto.response.QuizResponse;
import edu.platform.modules.quiz.entity.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CourseMapper.class, QuestionMapper.class})
public interface QuizMapper {
    
    QuizResponse toResponse(Quiz quiz);
    
    List<QuizResponse> toResponseList(List<Quiz> quizzes);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "attempts", ignore = true)
    Quiz toEntity(QuizRequest request);
}





