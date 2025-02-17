package com.example.news.controllers;

import com.example.news.dtos.ArticleDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.responses.ArticleResponse;
import com.example.news.responses.CustomPageResponse;
import com.example.news.services.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/article")
public class ArticleController {

    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping("/create")
    public ResponseEntity<ArticleResponse> createArticle(@RequestBody ArticleDTO articleDTO) {
        ArticleResponse createdArticle = articleService.createArticle(articleDTO);
        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable("id") Long id,
                                           @RequestBody ArticleDTO articleDTO) {
        ArticleResponse updatedArticle = articleService.updateArticle(id, articleDTO);
        return ResponseEntity.ok(updatedArticle);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<ArticleResponse> getArticle(@PathVariable Long id) {
        ArticleResponse article = articleService.getArticle(id);
        return ResponseEntity.ok(article);
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @GetMapping("/filter/page")
    public ResponseEntity<?> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "all", value = "filter") String filter,
            @RequestParam(defaultValue = "1", value = "category") Long categoryId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.getPublishedArticles(keyword, filter, categoryId, page, size);
        return ResponseEntity.ok(new CustomPageResponse<>(articles));
    }
    @GetMapping("/draft")
    public ResponseEntity<?> draftArticle(
            @RequestParam("id") Long id
    ){
        return ResponseEntity.ok(this.articleService.moveToDraft(id));
    }
    @PostMapping("/match/tag")
    public ResponseEntity<?> matchTagArticles(
            @RequestParam("articleId") Long articleId,
            @RequestParam("tagId") Long tagId
    ){
        return ResponseEntity.ok(this.articleService.matchTagArticle(articleId, tagId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.getArticlesByCategory(categoryId, page, size);
        return ResponseEntity.ok(new CustomPageResponse<>(articles));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getArticlesByStatus(
            @PathVariable ArticleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.getArticlesByStatus(status, page, size);
        return ResponseEntity.ok(new CustomPageResponse<>(articles));
    }

    @PostMapping("/increment/{id}")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ArticleResponse> articles = articleService.searchArticles(keyword, page, size);
        return ResponseEntity.ok(new CustomPageResponse<>(articles));
    }
}

