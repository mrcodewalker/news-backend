package com.example.news.controllers;

import com.example.news.dtos.ArticleDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.ArticleResponse;
import com.example.news.responses.ArticleDetailResponse;
import com.example.news.responses.HomeResponse;
import com.example.news.responses.ListDataResponse;
import com.example.news.services.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article")
@Tag(name = "Article Management", description = "APIs for managing articles")
public class ArticleController {
    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "Create a new article", description = "Creates a new article with the provided details")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Article created successfully",
            content = @Content(schema = @Schema(implementation = ArticleResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(@RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.createArticle(articleDTO));
    }

    @GetMapping("/by_tags")
    public ResponseEntity<ApiResponse<ListDataResponse>> filterByTags(){
        return ResponseEntity.ok(this.articleService.getSpecialArticle());
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<HomeResponse>> homeData(){
        return ResponseEntity.ok(this.articleService.getHomeData());
    }

    @GetMapping("/by_sub")
    public ResponseEntity<ApiResponse<ListDataResponse>> filterBySubCategoryId(@RequestParam("subCategoryId") Long subCategoryId){
        return ResponseEntity.ok(this.articleService.getArticleBySubCategoryId(subCategoryId));
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable("id") Long id,
            @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(articleService.updateArticle(id, articleDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.deleteArticle(id));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticle(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getArticleBySlug(slug));
    }

    @GetMapping("/hot/{slug}")
    public ResponseEntity<ApiResponse<ArticleDetailResponse>> getHotArticleBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getHotArticleBySlug(slug));
    }

    @GetMapping("/star")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getStar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.getHotArticles(page, size));
    }

    @GetMapping("/filter/page")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "all", value = "filter") String filter,
            @RequestParam(defaultValue = "1", value = "category") Long categoryId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.getPublishedArticles(keyword, filter, categoryId, page, size));
    }

    @GetMapping("/paging")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getPublishedArticlesPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1", value = "slug") String slug,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.getArticlesByCategorySlug(slug, page, size));
    }

    @GetMapping("/draft")
    public ResponseEntity<ApiResponse<ArticleResponse>> draftArticle(@RequestParam("id") Long id){
        return ResponseEntity.ok(this.articleService.moveToDraft(id));
    }

    @PostMapping("/match/tag")
    public ResponseEntity<ApiResponse<ArticleResponse>> matchTagArticles(
            @RequestParam("articleId") Long articleId,
            @RequestParam("tagId") Long tagId){
        return ResponseEntity.ok(this.articleService.matchTagArticle(articleId, tagId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.getArticlesByCategory(categoryId, page, size));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> getArticlesByStatus(
            @PathVariable ArticleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.getArticlesByStatus(status, page, size));
    }

    @PostMapping("/increment/{id}")
    public ResponseEntity<ApiResponse<Void>> incrementViewCount(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.incrementViewCount(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ArticleResponse>>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(articleService.searchArticles(keyword, page, size));
    }
}

