package edu.platform.modules.user.mapper;

import edu.platform.modules.user.dto.request.UserRegistrationRequest;
import edu.platform.modules.user.dto.response.UserResponse;
import edu.platform.modules.user.entity.User;
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


