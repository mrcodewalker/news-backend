package com.example.news.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDataResponse {
    @JsonProperty("tag")
    private String tagName;
    @JsonProperty("data")
    private List<ArticleTagResponse> list;
}
