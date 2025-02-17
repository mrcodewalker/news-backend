package com.example.news.responses;

import com.example.news.dtos.TagDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.models.Tag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String fileUrl;
    private Long fileId;
    private String fileName;
    private Long thumbnailId;
    private Set<Tag> tag;
    private String thumbnailUrl;
    private String dimensions;
    private Integer viewCount;
    private ArticleStatus status;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
}
