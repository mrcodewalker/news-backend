package com.example.news.repositories;

import com.example.news.models.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Long> {
    List<SubCategory> findByCategoryId(Long categoryId);
    @Query("SELECT s.id FROM SubCategory s")
    List<Long> findAllIds();

    Optional<SubCategory> findBySlug(String slug);
    boolean existsByNameAndCategoryId(String name, Long categoryId);
}
