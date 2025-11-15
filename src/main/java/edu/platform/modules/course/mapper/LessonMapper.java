package edu.platform.modules.course.mapper;

import edu.platform.modules.media.mapper.MediaAssetMapper;
import edu.platform.modules.course.dto.request.LessonRequest;
import edu.platform.modules.course.dto.response.LessonResponse;
import edu.platform.modules.course.entity.Lesson;
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




