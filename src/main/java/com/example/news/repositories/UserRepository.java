package com.example.news.repositories;

import com.example.news.models.Article;
import com.example.news.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.isActive = :active " +
            "ORDER BY u.createdAt DESC")
    Page<User> findUsers(@Param("active") boolean active, Pageable pageable);
}
