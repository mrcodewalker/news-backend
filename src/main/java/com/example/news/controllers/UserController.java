package com.example.news.controllers;

import com.example.news.dtos.UserDTO;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.CustomPageResponse;
import com.example.news.responses.UserResponse;
import com.example.news.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.register(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.login(userDTO));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getArticlesByStatus(
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.pagingUser(active, page, size));
    }

    @DeleteMapping("/delete/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.deleteUser(username));
    }

    @PutMapping("/update/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable String username, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(username, userDTO));
    }
}
