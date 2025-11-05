package edu.platform.service;

import edu.platform.dto.response.MediaAssetResponse;
import edu.platform.entity.MediaAsset;
import edu.platform.entity.User;
import edu.platform.exception.ResourceNotFoundException;
import edu.platform.mapper.MediaAssetMapper;
import edu.platform.repository.MediaAssetRepository;
import edu.platform.repository.UserRepository;
import edu.platform.storage.FileStorage;
import edu.platform.storage.StoredFileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaAssetService {
    
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;
    private final MediaAssetMapper mediaAssetMapper;
    private final FileStorage fileStorage;
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public MediaAssetResponse uploadFile(MultipartFile file, Long ownerId) throws IOException {
        log.debug("Uploading file: {} for owner: {}", file.getOriginalFilename(), ownerId);
        
        StoredFileInfo storedFileInfo;
        try {
            storedFileInfo = fileStorage.save(file, ownerId);
        } catch (IOException e) {
            log.error("Failed to save file to storage", e);
            throw new RuntimeException("Failed to save file", e);
        }
        
        return saveMediaAssetMetadata(storedFileInfo, ownerId);
    }
    
    private MediaAssetResponse saveMediaAssetMetadata(StoredFileInfo storedFileInfo, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + ownerId));
        
        // Check if file with same checksum already exists
        return mediaAssetRepository.findByChecksum(storedFileInfo.getChecksum())
                .map(existing -> {
                    log.info("File with checksum {} already exists, returning existing entry", storedFileInfo.getChecksum());
                    return mediaAssetMapper.toResponse(existing);
                })
                .orElseGet(() -> {
                    MediaAsset mediaAsset = MediaAsset.builder()
                            .owner(owner)
                            .fileName(storedFileInfo.getFileName())
                            .filePath(storedFileInfo.getFilePath())
                            .fileSize(storedFileInfo.getFileSize())
                            .contentType(storedFileInfo.getContentType())
                            .checksum(storedFileInfo.getChecksum())
                            .build();
                    
                    MediaAsset saved = mediaAssetRepository.save(mediaAsset);
                    log.info("Media asset saved with ID: {}", saved.getId());
                    
                    return mediaAssetMapper.toResponse(saved);
                });
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public MediaAssetResponse getMediaAssetById(Long id) {
        log.debug("Fetching media asset by ID: {}", id);
        
        MediaAsset mediaAsset = mediaAssetRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
        
        return mediaAssetMapper.toResponse(mediaAsset);
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<MediaAssetResponse> getMediaAssetsByOwner(Long ownerId) {
        log.debug("Fetching media assets for owner: {}", ownerId);
        
        List<MediaAsset> mediaAssets = mediaAssetRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
        return mediaAssetMapper.toResponseList(mediaAssets);
    }
    
    public Resource downloadFile(Long id) throws IOException {
        log.debug("Downloading file for media asset ID: {}", id);
        
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
        
        return fileStorage.load(mediaAsset.getFilePath());
    }
    
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE)
    public void deleteMediaAsset(Long id, Long userId) throws IOException {
        log.debug("Deleting media asset ID: {} by user: {}", id, userId);
        
        MediaAsset mediaAsset = mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
        
        // Check ownership
        if (!mediaAsset.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("User can only delete their own files");
        }
        
        // Delete file from storage
        fileStorage.delete(mediaAsset.getFilePath());
        
        // Delete metadata from database
        mediaAssetRepository.delete(mediaAsset);
        
        log.info("Media asset deleted successfully: {}", id);
    }
    
    // Internal method for other services
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public MediaAsset getMediaAssetEntityById(Long id) {
        return mediaAssetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media asset not found with ID: " + id));
    }
    
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<MediaAsset> getMediaAssetEntitiesByIds(List<Long> ids) {
        return mediaAssetRepository.findAllById(ids);
    }
}