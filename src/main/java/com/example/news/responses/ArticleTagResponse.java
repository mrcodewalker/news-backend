package com.example.news.responses;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleTagResponse {
    private Long articleId;
    private String title;
    private String slug;
    private String content;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long tagId;
    private String tagName;
    private Long subCategoryId;
    private String subCategoryName;
    private Long viewCounts;
    private String subCategorySlug;
}
