package com.example.news.dtos;

import com.example.news.enums.ArticleStatus;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDTO {
    private Long categoryId;
    private String title;
    private String summary;
    private String content;
    private Long thumbnailId;
    private Long fileId;
    private List<Long> tags;
    private ArticleStatus status;
}
