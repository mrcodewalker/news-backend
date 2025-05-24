package com.example.news.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
    @JsonProperty("special")
    private ListDataResponse listDataResponse;
    @JsonProperty("list")
    private List<ListDataResponse> responseList;
}
