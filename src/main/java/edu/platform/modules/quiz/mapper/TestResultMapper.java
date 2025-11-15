package edu.platform.modules.quiz.mapper;

import edu.platform.modules.user.mapper.UserMapper;
import edu.platform.modules.quiz.dto.response.TestResultResponse;
import edu.platform.modules.quiz.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, QuizMapper.class})
public interface TestResultMapper {
    
    @Mapping(source = "quizAttempt.id", target = "quizAttemptId")
    @Mapping(target = "formattedTimeSpent", expression = "java(testResult.getFormattedTimeSpent())")
    @Mapping(target = "passed", expression = "java(testResult.isPassed())")
    TestResultResponse toResponse(TestResult testResult);
    
    List<TestResultResponse> toResponseList(List<TestResult> testResults);
}




