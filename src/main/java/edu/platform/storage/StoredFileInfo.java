package edu.platform.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains information about a stored file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileInfo {
    
    /**
     * Original file name
     */
    private String fileName;
    
    /**
     * Path where file is stored
     */
    private String filePath;
    
    /**
     * File size in bytes
     */
    private Long fileSize;
    
    /**
     * Content type (MIME type)
     */
    private String contentType;
    
    /**
     * SHA-256 checksum of the file
     */
    private String checksum;
}