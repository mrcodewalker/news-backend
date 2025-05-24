package com.example.news.services;

import com.example.news.handler.ResourceNotFoundException;
import com.example.news.models.MediaFile;
import com.example.news.repositories.MediaFileRepository;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.MediaFileResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

    public ApiResponse<MediaFileResponse> uploadFile(MultipartFile file) throws IOException {
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
        if (mediaFileData.isPresent()) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "File with this name already exists");
        }

        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetLocation = dateDirectory.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        MediaFile mediaFile = new MediaFile();
        mediaFile.setOriginalName(originalFilename);
        mediaFile.setFilePath(targetLocation.toString());
        mediaFile.setFileType(file.getContentType());
        mediaFile.setFileSize(file.getSize());

        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image != null) {
                mediaFile.setDimensions(image.getWidth() + "x" + image.getHeight());
            }
        }

        MediaFile savedFile = mediaFileRepository.save(mediaFile);
        return ApiResponse.created(convertToResponse(savedFile), "File uploaded successfully");
    }

    public ApiResponse<MediaFileResponse> getFileById(Long id) {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        return ApiResponse.success(convertToResponse(file));
    }

    public ApiResponse<List<MediaFileResponse>> getAllFiles() {
        List<MediaFile> files = mediaFileRepository.findAll();
        return ApiResponse.success(files.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
    }

    public ApiResponse<Void> deleteFile(Long id) {
        MediaFile file = mediaFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        try {
            Files.deleteIfExists(Paths.get(file.getFilePath()));
            mediaFileRepository.deleteById(id);
            return ApiResponse.success(null, "File deleted successfully");
        } catch (IOException e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting file: " + e.getMessage());
        }
    }

    private MediaFileResponse convertToResponse(MediaFile file) {
        return MediaFileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .filePath(file.getFilePath())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .dimensions(file.getDimensions())
                .build();
    }
}
