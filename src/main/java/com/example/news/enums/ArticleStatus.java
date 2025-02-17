package com.example.news.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArticleStatus {
    DRAFT("draft"), PUBLISHED("published");
    private final String value;
    ArticleStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ArticleStatus fromValue(String value) {
        for (ArticleStatus status : ArticleStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ArticleStatus: " + value);
    }
}
