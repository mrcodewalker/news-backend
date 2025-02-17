package com.example.news.dtos;

import lombok.*;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private boolean active;
    private String role;
    private String username;
    private String password;
}
