package edu.platform.modules.media.repository;

import edu.platform.modules.media.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    
    Optional<MediaAsset> findByChecksum(String checksum);
    
    List<MediaAsset> findByOwnerId(Long ownerId);
    
    @Query("SELECT ma FROM MediaAsset ma WHERE ma.owner.id = :ownerId ORDER BY ma.createdAt DESC")
    List<MediaAsset> findByOwnerIdOrderByCreatedAtDesc(@Param("ownerId") Long ownerId);
    
    Optional<MediaAsset> findByFilePath(String filePath);
    
    @Query("SELECT ma FROM MediaAsset ma JOIN FETCH ma.owner WHERE ma.id = :id")
    Optional<MediaAsset> findByIdWithOwner(@Param("id") Long id);
    
    boolean existsByChecksum(String checksum);
    
    @Query("SELECT COUNT(ma) FROM MediaAsset ma WHERE ma.owner.id = :ownerId")
    Long countByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT SUM(ma.fileSize) FROM MediaAsset ma WHERE ma.owner.id = :ownerId")
    Long getTotalFileSizeByOwnerId(@Param("ownerId") Long ownerId);
}
