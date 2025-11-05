package edu.platform.mapper;

import edu.platform.dto.response.MediaAssetResponse;
import edu.platform.entity.MediaAsset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MediaAssetMapper {
    
    @Mapping(target = "downloadUrl", expression = "java(\"/api/media/\" + mediaAsset.getId() + \"/download\")")
    MediaAssetResponse toResponse(MediaAsset mediaAsset);
    
    List<MediaAssetResponse> toResponseList(List<MediaAsset> mediaAssets);
}