package com.example.news.controllers;

import com.example.news.dtos.UserDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.responses.ArticleResponse;
import com.example.news.responses.CustomPageResponse;
import com.example.news.responses.UserResponse;
import com.example.news.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        UserResponse response = userService.register(userDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO) {
        UserResponse response = userService.login(userDTO);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/filter")
    public ResponseEntity<?> getArticlesByStatus(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserResponse> userResponses = userService.pagingUser(active, page, size);
        return ResponseEntity.ok(new CustomPageResponse<>(userResponses));
    }

    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody UserDTO userDTO) {
        UserResponse response = userService.updateUser(username, userDTO);
        return ResponseEntity.ok(response);
    }
}
