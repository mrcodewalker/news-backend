package com.example.news.dtos;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaFileDTO {
    private String fileName;
    private String originalName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String mimeType;
    private String dimensions;
}
