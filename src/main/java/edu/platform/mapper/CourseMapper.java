package edu.platform.mapper;

import edu.platform.dto.request.CourseRequest;
import edu.platform.dto.response.CourseResponse;
import edu.platform.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CourseMapper {
    
    CourseResponse toResponse(Course course);
    
    List<CourseResponse> toResponseList(List<Course> courses);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "teacher", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "quizzes", ignore = true)
    Course toEntity(CourseRequest request);
}
