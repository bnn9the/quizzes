package edu.platform.mapper;

import edu.platform.dto.response.CourseVisitResponse;
import edu.platform.entity.CourseVisit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseVisitMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "lesson.id", target = "lessonId")
    @Mapping(source = "quiz.id", target = "quizId")
    CourseVisitResponse toResponse(CourseVisit visit);

    List<CourseVisitResponse> toResponseList(List<CourseVisit> visits);
}
