package edu.platform.modules.media.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Media asset upload request")
public class MediaAssetRequest {
    
    @NotNull(message = "File is required")
    @Schema(description = "File to upload (PNG, JPG, WEBP, max 5MB)", required = true)
    private MultipartFile file;
    
    @Schema(description = "Description of the file", example = "Course cover image")
    private String description;
}