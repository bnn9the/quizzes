package edu.platform.mapper;

import edu.platform.dto.request.UserRegistrationRequest;
import edu.platform.dto.response.UserResponse;
import edu.platform.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponse toResponse(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "teachingCourses", ignore = true)
    @Mapping(target = "quizAttempts", ignore = true)
    User toEntity(UserRegistrationRequest request);
}
