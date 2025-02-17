package com.example.news.services;

import com.example.news.dtos.UserDTO;
import com.example.news.filters.JwtUtil;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.models.Article;
import com.example.news.models.User;
import com.example.news.repositories.UserRepository;
import com.example.news.responses.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse login(UserDTO userDTO) {
        User user = this.userRepository.findByUsername(userDTO.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()){
            throw new ResourceNotFoundException("User not found");
        }
        if (!passwordEncoder.matches(userDTO.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);

        return new UserResponse(user.getId(), user.getUsername(), token, user.isActive(), user.getCreatedAt(), user.getRole().name());
    }
    public UserResponse register(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if (userDTO.getRole()!=null) {
            user.setRole(User.Role.valueOf(userDTO.getRole()));
        } else {
            user.setRole(User.Role.USER);
        }
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new UserResponse(user.getId(), user.getUsername(), token, user.isActive(), user.getCreatedAt(), user.getRole().name());
    }
    public Page<UserResponse> pagingUser(boolean active, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findUsers(active, pageable);
        return users.map(this::mapToResponse);
    }
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(false);
        this.userRepository.save(user);
    }

    public UserResponse updateUser(String username, UserDTO userDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userDTO.getUsername() != null && !userDTO.getUsername().equals(username)) {
            if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new IllegalArgumentException("New username already exists");
            }
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.isActive()!=user.isActive()){
            user.setActive(userDTO.isActive());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getRole() != null){
            user.setRole(User.Role.valueOf(userDTO.getRole()));
        }

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new UserResponse(user.getId(), user.getUsername(), token, user.isActive(), user.getCreatedAt(), user.getRole().name());
    }
    private UserResponse mapToResponse(User user){
        return UserResponse.builder()
                .active(user.isActive())
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .username(user.getUsername())
                .build();
    }
}
