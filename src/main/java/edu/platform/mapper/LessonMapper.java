package edu.platform.mapper;

import edu.platform.dto.request.LessonRequest;
import edu.platform.dto.response.LessonResponse;
import edu.platform.entity.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaAssetMapper.class})
public interface LessonMapper {
    
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseTitle")
    LessonResponse toResponse(Lesson lesson);
    
    List<LessonResponse> toResponseList(List<Lesson> lessons);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "mediaAssets", ignore = true)
    Lesson toEntity(LessonRequest request);
}