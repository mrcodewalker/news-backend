package com.example.news.responses;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedResponse {
    private String thumbnailUrl;
    private Long thumbnailId;
}
