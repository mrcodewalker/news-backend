package com.example.news.services;

import com.example.news.dtos.ArticleDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.*;
import com.example.news.repositories.*;
import com.example.news.responses.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.metrics.StartupStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MediaFileRepository mediaFileRepository;
    private final SlugGenerator slugGenerator;
    private final TagRepository tagRepository;
    private final SubCategoryRepository subCategoryRepository;
    @Autowired
    public ArticleService(ArticleRepository articleRepository,
                          CategoryRepository categoryRepository,
                          MediaFileRepository mediaFileRepository,
                          SlugGenerator slugGenerator,
                          TagRepository tagRepository,
                          SubCategoryRepository subCategoryRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.slugGenerator = slugGenerator;
        this.tagRepository = tagRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    public ApiResponse<ArticleResponse> createArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        if (articleDTO.getThumbnailId()==null){
            article.setThumbnail(this.mediaFileRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("No thumbnail default")));
        }
        if (articleDTO.getFolderUrl() != null){
            article.setUrl(
                    articleDTO.getFolderUrl());
        }
        if (articleDTO.getSubCategoryId()!=null){
            this.subCategoryRepository.findById(articleDTO.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Can not find sub category id"));
            article.setSubCategoryId(articleDTO.getSubCategoryId());
        }
        if (articleDTO.getTitle()!=null) {
            article.setTitle(articleDTO.getTitle());
            article.setSlug(slugGenerator.generateSlug(articleDTO.getTitle()));
            if (this.articleRepository.findBySlug(article.getSlug()).isPresent()) {
                article.setSlug(article.getSlug() + "-" + UUID.randomUUID().toString().substring(0, 7));
            }
        }
        //        if (articleRepository.findByTitle(articleDTO.getTitle()).isPresent()){
//            throw new ResourceNotFoundException("Title has been existed in database");
//        }
        article.setTitle(articleDTO.getTitle());
        article.setSummary(articleDTO.getSummary());
        article.setContent(articleDTO.getContent());
        List<Tag> list = this.tagRepository.findAllById(articleDTO.getTags());
        if (list.size()>0){
            article.setTags(new HashSet<>(list));
        }
        if (articleDTO.getStatus().equals(ArticleStatus.PUBLISHED)){
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        if (articleDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(articleDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            article.setCategory(category);
        }

        if (articleDTO.getThumbnailId() != null) {
            MediaFile thumbnail = mediaFileRepository.findById(articleDTO.getThumbnailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Thumbnail not found"));
            article.setThumbnail(thumbnail);
        }
        if (articleDTO.getFileId() != null){
            MediaFile file = mediaFileRepository.findById(articleDTO.getFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("File not found"));
            article.setFileId(file.getId());
        }

        return ApiResponse.created(convertToResponse(articleRepository.save(article)), "Article created successfully");
    }
    @Transactional
    public ApiResponse<ArticleResponse> updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        if (articleDTO.getFolderUrl() != null){
            article.setUrl(
                    articleDTO.getFolderUrl());
        }
        if (articleDTO.getSubCategoryId()!=null){
            this.subCategoryRepository.findById(articleDTO.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Can not find sub category id"));
            article.setSubCategoryId(articleDTO.getSubCategoryId());
        }
        if (articleDTO.getTitle()!=null) {
            if (!articleDTO.getTitle().equalsIgnoreCase(article.getTitle())) {
                article.setTitle(articleDTO.getTitle());
                article.setSlug(slugGenerator.generateSlug(articleDTO.getTitle()));
                if (this.articleRepository.findBySlug(article.getSlug()).isPresent()) {
                    article.setSlug(article.getSlug() + "-" + UUID.randomUUID().toString().substring(0, 7));
                }
            }
        }
        if (articleDTO.getSummary()!=null) {
            article.setSummary(articleDTO.getSummary());
        }
        if (articleDTO.getContent()!=null) {
            article.setContent(articleDTO.getContent());
        }
        if (articleDTO.getThumbnailId() != null) {
            MediaFile thumbnail = mediaFileRepository.findById(articleDTO.getThumbnailId())
                    .orElseThrow(() -> new ResourceNotFoundException("Thumbnail not found"));
            article.setThumbnail(thumbnail);
        }
        if (articleDTO.getCategoryId()!=null){
            Category category = categoryRepository.findById(articleDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            article.setCategory(category);
        }
        if (articleDTO.getTags()!=null){
            List<Tag> list = this.tagRepository.findAllById(articleDTO.getTags());
            article.setTags(new HashSet<>(list));
        }
        if (articleDTO.getStatus().equals(ArticleStatus.PUBLISHED)) {
            article.setPublishedAt(LocalDateTime.now());
            article.setStatus(ArticleStatus.PUBLISHED);
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        article.setFileId(articleDTO.getFileId());
        return ApiResponse.success(convertToResponse(articleRepository.save(article)), "Article updated successfully");
    }

    public ApiResponse<Void> deleteArticle(Long id) {
        articleRepository.deleteById(id);
        return ApiResponse.success(null, "Article deleted successfully");
    }

    public ApiResponse<ArticleResponse> getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return ApiResponse.success(convertToResponse(article));
    }
    public ApiResponse<Page<ArticleResponse>> getArticlesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findByCategoryId(categoryId, pageable);
        return ApiResponse.success(articlePage.map(this::convertToResponse));
    }

    public ApiResponse<Page<ArticleResponse>> getArticlesByStatus(ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Article> articlePage = articleRepository.findByStatus(status, pageable);
        return ApiResponse.success(articlePage.map(this::convertToResponse));
    }

    @Transactional
    public ApiResponse<Void> incrementViewCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        return ApiResponse.success(null, "View count incremented successfully");
    }

    public ApiResponse<Page<ArticleResponse>> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.searchArticles(keyword, pageable);
        return ApiResponse.success(articlePage.map(this::convertToResponse));
    }
    public ApiResponse<ArticleDetailResponse> getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount()+1);
        this.articleRepository.save(article);
        List<Long> tagIds = article.getTags().stream().map(Tag::getId).toList();
        if (article.getSubCategoryId()!=null){
            return ApiResponse.success(ArticleDetailResponse.builder()
                    .articleResponse(convertToResponse(article))
                    .list(articleRepository.findArticlesBySubCategory(
                            article.getSubCategoryId(),
                             article.getId(),
                            PageRequest.of(0, 10)
                    )
                            .stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList()))
                    .build());
        }
        return ApiResponse.success(ArticleDetailResponse.builder()
                .articleResponse(convertToResponse(article))
                .list(articleRepository.findArticlesByCategory(
                                article.getCategory().getId(), article.getId(),
                                PageRequest.of(0, 10))
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()))
                .build());
    }
    public ApiResponse<ArticleDetailResponse> getHotArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount()+1);
        this.articleRepository.save(article);
        List<Long> tagIds = article.getTags().stream().map(Tag::getId).toList();
        if (article.getTags().size()>0){
            return ApiResponse.success(ArticleDetailResponse.builder()
                    .articleResponse(convertToResponse(article))
                    .list(articleRepository.findArticlesWithTagsExcludingCurrent(
                                    article.getId(),
                                    PageRequest.of(0, 10)
                            )
                            .stream()
                            .map(this::convertToResponse)
                            .collect(Collectors.toList()))
                    .build());
        }
        return this.getArticleBySlug(slug);
    }
    public ApiResponse<ArticleResponse> moveToDraft(Long articleId){
        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find article with article ID"));
        article.setStatus(ArticleStatus.DRAFT);
        return ApiResponse.success(convertToResponse(this.articleRepository.save(article)), "Article moved to draft successfully");
    }
    public ApiResponse<Page<ArticleResponse>> getPublishedArticles(String keyword, String filter, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Article> articlePage = null;
        if (categoryId==0){
            if (filter.equalsIgnoreCase("all")){
                articlePage = articleRepository.findArticles(pageable);
            } else if (filter.equalsIgnoreCase("published")){
                articlePage = articleRepository.findPublishedArticles(pageable);
            } else articlePage = articleRepository.findDraftArticles(pageable);
            return ApiResponse.success(articlePage.map(this::convertToResponse));
        }
        if (keyword.length()>0){
            return this.searchArticles(keyword, page, size);
        }
        if (filter.equalsIgnoreCase("all")){
            articlePage = articleRepository.findArticlesCategory(pageable, categoryId);
        } else if (filter.equalsIgnoreCase("published")){
            articlePage = articleRepository.findPublishedArticlesCategory(pageable, categoryId);
        } else articlePage = articleRepository.findDraftArticlesCategory(pageable, categoryId);
        return ApiResponse.success(articlePage.map(this::convertToResponse));
    }
    public ApiResponse<Page<ArticleResponse>> getHotArticles(int page, int size){
        return ApiResponse.success(this.articleRepository.findAllArticlesWithTagsByStatus(ArticleStatus.PUBLISHED,
                PageRequest.of(page, size, Sort.by("publishedAt").descending())).map(this::convertToResponse));
    }
    public ApiResponse<Page<ArticleResponse>> getArticlesByCategorySlug(String slug, int page, int size) {
        System.out.println("ALO 123");

        // Tìm theo subCategory trước
        Optional<SubCategory> subCategory = this.subCategoryRepository.findBySlug(slug);
        if (subCategory.isPresent()) {
            Page<Article> articlePage = this.articleRepository.findBySubCategory(
                    subCategory.get().getId(),
                    PageRequest.of(page, size, Sort.by("publishedAt").descending())
            );
            return ApiResponse.success(articlePage.map(this::convertToResponse));
        }

        Optional<Category> category = this.categoryRepository.findBySlug(slug);
        if (category.isPresent()) {
            Page<Article> articlePage = this.articleRepository.findByCategory(
                    category.get().getId(),
                    PageRequest.of(page, size, Sort.by("publishedAt").descending())
            );
            return ApiResponse.success(articlePage.map(this::convertToResponse));
        }

        return ApiResponse.notFound("No category or subcategory found with slug: " + slug);
    }
    public ApiResponse<Page<ArticleResponse>> getDraftArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Article> articlePage = articleRepository.findDraftArticles(pageable);
        return ApiResponse.success(articlePage.map(this::convertToResponse));
    }
    public ApiResponse<ArticleResponse> matchTagArticle(Long articleId,
                                            Long tagId){
        Tag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find tag"));
        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find article"));
        Set<Tag> tags = article.getTags();
        tags.add(tag);
        article.setTags(tags);
        return ApiResponse.success(this.convertToResponse(this.articleRepository.save(article)), "Tag matched successfully");
    }
    public ApiResponse<ListDataResponse> getSpecialArticle(){
        return ApiResponse.success(ListDataResponse.builder()
                .list(
                this.articleRepository.mapToArticleResponses(this.articleRepository.findLatestArticlesWithTags()))
                .tagName("Nổi bật")
        .build());
    }
    public ApiResponse<ListDataResponse> getArticleBySubCategoryId(Long subCategoryId){
        SubCategory subCategory = this.subCategoryRepository.findById(subCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find sub category with id"));
        return ApiResponse.success(ListDataResponse.builder()
                .list(
                        this.articleRepository.mapToArticleResponses(this.articleRepository.findArticlesBySubCategory(subCategoryId)))
                .tagName(subCategory.getName())
                .build());
    }
    public ApiResponse<HomeResponse> getHomeData(){
        List<Long> subCategoryList = this.subCategoryRepository.findAllIds();
        subCategoryList.sort(Long::compare);
        List<ListDataResponse> data = new ArrayList<>();
        HomeResponse response = new HomeResponse();
        response.setListDataResponse(this.getSpecialArticle().getData());
        for (Long index: subCategoryList){
            data.add(
                    this.getArticleBySubCategoryId(index).getData()
            );
        }
        response.setResponseList(data);
        return ApiResponse.success(response);
    }
    private ArticleResponse convertToResponse(Article article) {
        String fileUrl = null;
        Long fileId = 0L;
        String fileName = null;
        if (article.getFileId()!=null){
            MediaFile file = this.mediaFileRepository.findById(article.getFileId())
                    .orElseThrow(() -> new ResourceNotFoundException("No file found"));
            fileUrl = file.getFilePath();
            fileId = file.getId();
            fileName = file.getOriginalName();
        }
        String subCategoryName = null;
        if (article.getSubCategoryId()!=null) {
            SubCategory subCategory = this.subCategoryRepository.findById(article.getSubCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Can not find sub category id"));
            subCategoryName = subCategory.getName();
        }
        return ArticleResponse.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .folderUrl(article.getUrl())
                .subCategoryId(article.getSubCategoryId())
                .title(article.getTitle())
                .content(article.getContent())
                .subCategoryName(subCategoryName)
                .categoryId(article.getCategory().getId())
                .publishedAt(article.getPublishedAt())
                .summary(article.getSummary())
                .status(article.getStatus())
                .fileUrl(fileUrl)
                .fileId(fileId)
                .fileName(fileName)
                .thumbnailId(article.getThumbnail().getId())
                .thumbnailUrl(article.getThumbnail().getFilePath())
                .viewCount(article.getViewCount())
                .categoryName(article.getCategory().getName())
                .dimensions(article.getThumbnail().getDimensions())
                .updatedAt(article.getUpdatedAt())
                .tag(article.getTags())
                .build();
    }
}
