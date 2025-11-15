package edu.platform.modules.media.mapper;

import edu.platform.modules.user.mapper.UserMapper;
import edu.platform.modules.media.dto.response.MediaAssetResponse;
import edu.platform.modules.media.entity.MediaAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MediaAssetMapper {
    
    @Mapping(target = "downloadUrl", expression = "java(\"/api/media/\" + mediaAsset.getId() + \"/download\")")
    MediaAssetResponse toResponse(MediaAsset mediaAsset);
    
    List<MediaAssetResponse> toResponseList(List<MediaAsset> mediaAssets);
}




