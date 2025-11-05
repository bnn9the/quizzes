package edu.platform.controller;

import edu.platform.dto.response.MediaAssetResponse;
import edu.platform.service.MediaAssetService;
import edu.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Media Management", description = "Media asset management APIs")
@SecurityRequirement(name = "bearerAuth")
public class MediaController {
    
    private final MediaAssetService mediaAssetService;
    private final UserService userService;
    
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload media file", description = "Upload an image file (PNG, JPG, WEBP, max 5MB)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "File uploaded successfully",
                content = @Content(schema = @Schema(implementation = MediaAssetResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file (wrong type or too large)"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<MediaAssetResponse> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long userId = userService.getCurrentUserEntity(email).getId();
        
        log.info("File upload request from user: {} (file: {})", email, file.getOriginalFilename());
        
        try {
            MediaAssetResponse response = mediaAssetService.uploadFile(file, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get media asset metadata", description = "Get metadata for a media asset by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media asset found",
                content = @Content(schema = @Schema(implementation = MediaAssetResponse.class))),
        @ApiResponse(responseCode = "404", description = "Media asset not found")
    })
    public ResponseEntity<MediaAssetResponse> getMediaAsset(
            @Parameter(description = "Media asset ID") @PathVariable Long id) {
        
        log.debug("Getting media asset metadata for ID: {}", id);
        
        MediaAssetResponse response = mediaAssetService.getMediaAssetById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/download")
    @Operation(summary = "Download media file", description = "Download the actual file content")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "File not found"),
        @ApiResponse(responseCode = "500", description = "Error reading file")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Media asset ID") @PathVariable Long id) {
        
        log.debug("Downloading file for media asset ID: {}", id);
        
        try {
            MediaAssetResponse metadata = mediaAssetService.getMediaAssetById(id);
            Resource resource = mediaAssetService.downloadFile(id);
            
            String contentType = metadata.getContentType();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + metadata.getFileName() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("File download failed for ID: {}", id, e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }
    
    @GetMapping("/my")
    @Operation(summary = "Get my uploaded files", description = "Get all files uploaded by current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files retrieved successfully")
    })
    public ResponseEntity<List<MediaAssetResponse>> getMyFiles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long userId = userService.getCurrentUserEntity(email).getId();
        
        log.debug("Getting files for user: {}", email);
        
        List<MediaAssetResponse> files = mediaAssetService.getMediaAssetsByOwner(userId);
        return ResponseEntity.ok(files);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete media file", description = "Delete a media file (only owner can delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "File deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Not the owner of this file"),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    public ResponseEntity<Void> deleteFile(
            @Parameter(description = "Media asset ID") @PathVariable Long id) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long userId = userService.getCurrentUserEntity(email).getId();
        
        log.info("Delete file request for ID: {} from user: {}", id, email);
        
        try {
            mediaAssetService.deleteMediaAsset(id, userId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            log.error("File deletion failed for ID: {}", id, e);
            throw new RuntimeException("File deletion failed: " + e.getMessage(), e);
        }
    }
}