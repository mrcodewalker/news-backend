package com.example.news.repositories;

import com.example.news.enums.ArticleStatus;
import com.example.news.models.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a " +
            "WHERE a.category.id = :categoryId " +
            "AND a.id <> :articleId AND a.status = 'PUBLISHED' " +
            "AND EXISTS (SELECT 1 FROM a.tags t WHERE t.id IN :tagIds) " +
            "ORDER BY a.publishedAt DESC " +
            "LIMIT 5")
    List<Article> findRelatedArticles(Long articleId, Long categoryId, List<Long> tagIds);
    Optional<Article> findBySlug(String slug);
    List<Article> findByCategoryId(Long categoryId);
    Optional<Article> findByTitle(String title);
    List<Article> findByStatus(ArticleStatus status);
    @Query("SELECT a FROM Article a LEFT JOIN MediaFile m ON m.id = a.fileId WHERE a.id = :id")
    Optional<Article> findArticleById(@Param("id") Long id);
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedArticles(Pageable pageable);
    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND a.category.id = :categoryId " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findPublishedArticlesCategory(Pageable pageable, @Param("categoryId") Long categoryId);
    @Query("SELECT a FROM Article a " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findArticles(Pageable pageable);
    @Query("SELECT a FROM Article a WHERE a.category.id = :categoryId " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findArticlesCategory(Pageable pageable, @Param("categoryId") Long categoryId);
    @Query("SELECT a FROM Article a WHERE a.status = 'DRAFT' " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findDraftArticles(Pageable pageable);
    @Query("SELECT a FROM Article a WHERE a.status = 'DRAFT' AND a.category.id = :categoryId " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findDraftArticlesCategory(Pageable pageable, @Param("categoryId") Long categoryId);
    Page<Article> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'PUBLISHED' AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Article> searchArticles(@Param("keyword") String keyword, Pageable pageable);
}
