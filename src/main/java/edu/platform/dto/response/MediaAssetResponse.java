package edu.platform.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Media asset response")
public class MediaAssetResponse {
    
    @Schema(description = "Media asset ID", example = "1")
    private Long id;
    
    @Schema(description = "Owner information")
    private UserResponse owner;
    
    @Schema(description = "Original file name", example = "course-cover.png")
    private String fileName;
    
    @Schema(description = "File path in storage", example = "/uploads/2024/11/abc123def456.png")
    private String filePath;
    
    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;
    
    @Schema(description = "Content type", example = "image/png")
    private String contentType;
    
    @Schema(description = "File checksum (SHA-256)", example = "abc123def456...")
    private String checksum;
    
    @Schema(description = "Upload timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Download URL", example = "/api/media/1/download")
    private String downloadUrl;
}