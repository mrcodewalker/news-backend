package com.example.news.services;

import com.example.news.handler.ResourceNotFoundException;
import com.example.news.models.MediaFile;
import com.example.news.repositories.MediaFileRepository;
import com.example.news.responses.MediaFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaFileService {
    private final MediaFileRepository mediaFileRepository;
    private final Path rootLocation;

    @Autowired
    public MediaFileService(@Value("${code.walker:uploads}") String uploadDir,
                            MediaFileRepository mediaFileRepository) {
        this.rootLocation = Paths.get(uploadDir);
        this.mediaFileRepository = mediaFileRepository;
    }

    public MediaFileResponse uploadFile(MultipartFile file) throws IOException {
        LocalDate today = LocalDate.now();
        String year = String.valueOf(today.getYear());
        String month = String.format("%02d", today.getMonthValue());
        String day = String.format("%02d", today.getDayOfMonth());

        Path dateDirectory = this.rootLocation.resolve(Paths.get(year, month, day));

        if (!Files.exists(dateDirectory)) {
            Files.createDirectories(dateDirectory);
        }

        String originalFilename = file.getOriginalFilename();
        Optional<MediaFile> mediaFileData = this.mediaFileRepository.getByOriginalName(originalFilename);
        if (mediaFileData.isEmpty()) {
            MediaFile mediaFile = new MediaFile();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            Path targetLocation = dateDirectory.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            mediaFile.setFileName(newFilename);
            mediaFile.setOriginalName(originalFilename);
            mediaFile.setFilePath(String.format("/uploads/%s/%s/%s/%s", year, month, day, newFilename));
            mediaFile.setFileType(extension.substring(1).toLowerCase());
            mediaFile.setFileSize(file.getSize());
            mediaFile.setMimeType(file.getContentType());

            if (file.getContentType().startsWith("image/")) {
                try (var inputStream = file.getInputStream()) {
                    BufferedImage img = ImageIO.read(inputStream);
                    if (img != null) {
                        mediaFile.setDimensions(img.getWidth() + "x" + img.getHeight());
                    }
                }
            }
            return convertToResponse(this.mediaFileRepository.save(mediaFile));
        }
        return convertToResponse(mediaFileData.get());
    }
    public List<MediaFileResponse> findPDFFIle(){
        return this.mediaFileRepository.findByFileType("pdf")
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<MediaFileResponse> findAllImages() {
        return this.mediaFileRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private MediaFileResponse convertToResponse(MediaFile mediaFile) {
        return MediaFileResponse.builder()
                .id(mediaFile.getId())
                .fileName(mediaFile.getFileName())
                .filePath(mediaFile.getFilePath())
                .fileSize(mediaFile.getFileSize())
                .fileType(mediaFile.getFileType())
                .createdAt(mediaFile.getCreatedAt())
                .mimeType(mediaFile.getMimeType())
                .originalName(mediaFile.getOriginalName())
                .dimensions(mediaFile.getDimensions())
                .build();
    }
}
