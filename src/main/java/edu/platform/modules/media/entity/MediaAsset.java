package edu.platform.modules.media.entity;

import edu.platform.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media_assets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaAsset {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, unique = true, length = 500)
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;
    
    @Column(nullable = false, length = 64)
    private String checksum;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
