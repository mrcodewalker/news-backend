package com.example.news.responses;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String token;
    private boolean active;
    private LocalDateTime createdAt;
    private String role;
}
