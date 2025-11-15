package edu.platform.modules.course.mapper;

import edu.platform.modules.course.dto.request.CourseRequest;
import edu.platform.modules.course.dto.response.CourseResponse;
import edu.platform.modules.course.entity.Course;
import edu.platform.modules.user.mapper.UserMapper;
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
    @Mapping(target = "lessons", ignore = true)
    @Mapping(target = "coverImages", ignore = true)
    Course toEntity(CourseRequest request);
}

