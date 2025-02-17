package com.example.news.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDetailResponse {
    @JsonProperty("response")
    private ArticleResponse articleResponse;
    @JsonProperty("related")
    private List<ArticleResponse> list;
}
