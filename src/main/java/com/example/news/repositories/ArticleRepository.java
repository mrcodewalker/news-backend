package com.example.news.repositories;

import com.example.news.enums.ArticleStatus;
import com.example.news.models.Article;
import com.example.news.responses.ArticleTagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a " +
            "WHERE a.category.id = :categoryId " +
            "AND a.id <> :articleId AND a.status = 'PUBLISHED' " +
            "AND EXISTS (SELECT 1 FROM a.tags t WHERE t.id IN :tagIds) " +
            "ORDER BY a.publishedAt DESC " +
            "LIMIT 5")
    List<Article> findRelatedArticles(Long articleId, Long categoryId, List<Long> tagIds);
    // Query for articles by subcategory
    @Query("SELECT DISTINCT a FROM Article a " +
            "WHERE a.subCategoryId = :subCategoryId " +
            "AND a.status = 'PUBLISHED' " +
            "AND a.id <> :articleId " +
            "ORDER BY a.publishedAt DESC")
    List<Article> findArticlesBySubCategory(
            @Param("subCategoryId") Long subCategoryId,
            @Param("articleId") Long articleId,
            Pageable pageable
    );
    @Query("SELECT DISTINCT a FROM Article a " +
            "JOIN a.tags t " +
            "WHERE a.status = 'PUBLISHED' " +
            "AND a.id <> :articleId " +
            "ORDER BY a.publishedAt DESC")
    List<Article> findArticlesWithTagsExcludingCurrent(
            @Param("articleId") Long articleId,
            Pageable pageable
    );
    @Query("SELECT DISTINCT a FROM Article a JOIN a.tags t WHERE a.status = :status")
    Page<Article> findAllArticlesWithTagsByStatus(ArticleStatus status, Pageable pageable);

    // Query for articles by category
    @Query("SELECT DISTINCT a FROM Article a " +
            "WHERE a.category.id = :categoryId " +
            "AND a.subCategoryId IS NULL " +
            "AND a.status = 'PUBLISHED' " +
            "AND a.id <> :articleId " +
            "ORDER BY a.publishedAt DESC")
    List<Article> findArticlesByCategory(
            @Param("categoryId") Long categoryId,
            @Param("articleId") Long articleId,
            Pageable pageable
    );
    @Query(value = """
            SELECT 
                t.tag_id, g.name, 
                s.id AS sub_category_id, s.name AS sub_category_name,
                a.id AS article_id, a.title, a.slug, a.content, a.summary, 
                a.created_at, a.updated_at, a.view_count, s.slug AS sub_category_slug 
            FROM articles a
            LEFT JOIN sub_categories s ON s.id = a.sub_category_id
            LEFT JOIN article_tags t ON t.article_id = a.id
            LEFT JOIN tags g ON g.id = t.tag_id
            WHERE a.status = 'PUBLISHED' 
            AND t.tag_id IS NOT NULL
            ORDER BY a.published_at DESC
            LIMIT 8
            """, nativeQuery = true)
    List<Object[]> findLatestArticlesWithTags();
    @Query(value = """
            SELECT 
                t.tag_id, g.name, 
                s.id AS sub_category_id, s.name AS sub_category_name,
                a.id AS article_id, a.title, a.slug, a.content, a.summary, 
                a.created_at, a.updated_at, a.view_count, s.slug AS sub_category_slug  
            FROM articles a
            LEFT JOIN sub_categories s ON s.id = a.sub_category_id
            LEFT JOIN article_tags t ON t.article_id = a.id
            LEFT JOIN tags g ON g.id = t.tag_id
            WHERE a.status = 'PUBLISHED' 
            AND a.sub_category_id = :subCategoryId
            ORDER BY a.sub_category_id ASC, a.published_at DESC
            LIMIT 8
            """, nativeQuery = true)
    List<Object[]> findArticlesBySubCategory(@Param("subCategoryId") Long subCategoryId);
    Optional<Article> findBySlug(String slug);
    @Query("SELECT a FROM Article a " +
            "WHERE a.subCategoryId = :subCategoryId " +
            "AND a.status = 'PUBLISHED' " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findBySubCategory(
            @Param("subCategoryId") Long subCategoryId,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a " +
            "WHERE a.category.id = :categoryId " +
            "AND a.status = 'PUBLISHED' " +
            "ORDER BY a.publishedAt DESC")
    Page<Article> findByCategory(
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

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
    default ArticleTagResponse mapToArticleResponse(Object[] result) {
        return ArticleTagResponse.builder()
                .tagId(result[0] != null ? ((Number) result[0]).longValue() : null)
                .tagName((String) result[1])
                .subCategoryId(result[2] != null ? ((Number) result[2]).longValue() : null)
                .subCategoryName((String) result[3])
                .articleId(result[4] != null ? ((Number) result[4]).longValue() : null)
                .title((String) result[5])
                .slug((String) result[6])
                .content((String) result[7])
                .summary((String) result[8])
                .createdAt(result[9] != null ? ((Timestamp) result[9]).toLocalDateTime() : null)
                .updatedAt(result[10] != null ? ((Timestamp) result[10]).toLocalDateTime() : null)
                .viewCounts(((Number) result[11]).longValue())
                .subCategorySlug((String) result[12])
                .build();
    }
    default List<ArticleTagResponse> mapToArticleResponses(List<Object[]> results) {
        return results.stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
    }
}
