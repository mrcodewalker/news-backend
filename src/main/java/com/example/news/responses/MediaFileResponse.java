package com.example.news.responses;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaFileResponse {
    private Long id;
    private String fileName;
    private String originalName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String dimensions;
    private LocalDateTime createdAt;
}
