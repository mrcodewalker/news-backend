package com.example.news.controllers;

import com.example.news.responses.ApiResponse;
import com.example.news.responses.MediaFileResponse;
import com.example.news.services.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/media_file")
@RequiredArgsConstructor
public class MediaFileController {
    private final MediaFileService mediaFileService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaFileResponse>> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(this.mediaFileService.uploadFile(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MediaFileResponse>> getFileById(@PathVariable Long id) {
        return ResponseEntity.ok(this.mediaFileService.getFileById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MediaFileResponse>>> getAllFiles() {
        return ResponseEntity.ok(this.mediaFileService.getAllFiles());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Long id) {
        return ResponseEntity.ok(this.mediaFileService.deleteFile(id));
    }

    @GetMapping("/uploads/{year}/{month}/{day}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        Path filePath = Paths.get(uploadDir, year, month, day, filename);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
