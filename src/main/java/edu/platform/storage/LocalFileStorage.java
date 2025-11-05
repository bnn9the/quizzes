package edu.platform.storage;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class LocalFileStorage implements FileStorage {
    
    private final Path rootLocation;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/png", "image/jpeg", "image/jpg", "image/webp"
    );
    
    public LocalFileStorage(@Value("${file.storage.location:uploads}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            log.info("File storage initialized at: {}", this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    @Override
    @CircuitBreaker(name = "fileStorage", fallbackMethod = "saveFallback")
    @Retry(name = "fileStorage")
    public StoredFileInfo save(MultipartFile file, Long ownerId) throws IOException {
        log.debug("Saving file: {} for owner: {}", file.getOriginalFilename(), ownerId);
        
        // Validate file
        validateFile(file);
        
        // Calculate checksum
        String checksum = calculateChecksum(file.getInputStream());
        
        // Generate file path based on date and checksum
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(fileName);
        String generatedFileName = checksum + extension;
        
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path targetLocation = rootLocation.resolve(yearMonth).resolve(generatedFileName);
        
        // Create directories if they don't exist
        Files.createDirectories(targetLocation.getParent());
        
        // Copy file to target location
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        }
        
        String relativePath = rootLocation.relativize(targetLocation).toString();
        
        log.info("File saved successfully: {} (checksum: {})", relativePath, checksum);
        
        return StoredFileInfo.builder()
                .fileName(fileName)
                .filePath(relativePath.replace("\\", "/")) // Normalize path separators
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .checksum(checksum)
                .build();
    }
    
    private StoredFileInfo saveFallback(MultipartFile file, Long ownerId, Exception e) {
        log.error("Circuit breaker activated for file save operation", e);
        throw new RuntimeException("File storage service is currently unavailable", e);
    }
    
    @Override
    @CircuitBreaker(name = "fileStorage", fallbackMethod = "loadFallback")
    @Retry(name = "fileStorage")
    public Resource load(String filePath) throws IOException {
        log.debug("Loading file: {}", filePath);
        
        Path file = rootLocation.resolve(filePath).normalize();
        Resource resource = new UrlResource(file.toUri());
        
        if (resource.exists() && resource.isReadable()) {
            log.debug("File loaded successfully: {}", filePath);
            return resource;
        } else {
            throw new IOException("File not found or not readable: " + filePath);
        }
    }
    
    private Resource loadFallback(String filePath, Exception e) {
        log.error("Circuit breaker activated for file load operation", e);
        throw new RuntimeException("File storage service is currently unavailable", e);
    }
    
    @Override
    @CircuitBreaker(name = "fileStorage", fallbackMethod = "deleteFallback")
    @Retry(name = "fileStorage")
    public void delete(String filePath) throws IOException {
        log.debug("Deleting file: {}", filePath);
        
        Path file = rootLocation.resolve(filePath).normalize();
        Files.deleteIfExists(file);
        
        log.info("File deleted successfully: {}", filePath);
    }
    
    private void deleteFallback(String filePath, Exception e) {
        log.error("Circuit breaker activated for file delete operation", e);
        throw new RuntimeException("File storage service is currently unavailable", e);
    }
    
    @Override
    public boolean exists(String filePath) {
        Path file = rootLocation.resolve(filePath).normalize();
        return Files.exists(file);
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "Invalid file type. Allowed types: PNG, JPG, WEBP. Got: " + contentType
            );
        }
    }
    
    private String calculateChecksum(InputStream inputStream) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }
}