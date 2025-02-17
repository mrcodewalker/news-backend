package com.example.news.repositories;

import com.example.news.models.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    @Query("SELECT m FROM MediaFile m WHERE m.fileType = :fileType ORDER BY m.updatedAt DESC")
    List<MediaFile> findByFileType(String fileType);
    boolean existsByOriginalName(String originalName);
    @Query("SELECT m FROM MediaFile m WHERE m.originalName = :originalName")
    Optional<MediaFile> getByOriginalName(String originalName);
}