package edu.platform.modules.media.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface for file storage operations.
 * Provides methods to save, load, and delete files.
 */
public interface FileStorage {
    
    /**
     * Save a file to storage.
     * Validates file type (PNG, JPG, WEBP) and size (max 5MB).
     * 
     * @param file the file to save
     * @param ownerId the ID of the user who owns the file
     * @return the saved file's metadata (path, checksum, etc.)
     * @throws IOException if file cannot be saved
     * @throws IllegalArgumentException if file is invalid
     */
    StoredFileInfo save(MultipartFile file, Long ownerId) throws IOException;
    
    /**
     * Load a file from storage as a Resource.
     * 
     * @param filePath the path to the file
     * @return Resource representing the file
     * @throws IOException if file cannot be loaded
     */
    Resource load(String filePath) throws IOException;
    
    /**
     * Delete a file from storage.
     * 
     * @param filePath the path to the file to delete
     * @throws IOException if file cannot be deleted
     */
    void delete(String filePath) throws IOException;
    
    /**
     * Check if a file exists in storage.
     * 
     * @param filePath the path to check
     * @return true if file exists, false otherwise
     */
    boolean exists(String filePath);
}