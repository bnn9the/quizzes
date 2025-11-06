package edu.platform.mapper;

import edu.platform.dto.response.TestResultResponse;
import edu.platform.entity.TestResult;
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