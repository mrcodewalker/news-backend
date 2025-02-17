package com.example.news.services;

import com.example.news.dtos.ArticleDTO;
import com.example.news.enums.ArticleStatus;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.Article;
import com.example.news.models.Category;
import com.example.news.models.MediaFile;
import com.example.news.models.Tag;
import com.example.news.repositories.ArticleRepository;
import com.example.news.repositories.CategoryRepository;
import com.example.news.repositories.MediaFileRepository;
import com.example.news.repositories.TagRepository;
import com.example.news.responses.ArticleDetailResponse;
import com.example.news.responses.ArticleResponse;
import com.example.news.responses.TagResponse;
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

    @Autowired
    public ArticleService(ArticleRepository articleRepository,
                          CategoryRepository categoryRepository,
                          MediaFileRepository mediaFileRepository,
                          SlugGenerator slugGenerator,
                          TagRepository tagRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.slugGenerator = slugGenerator;
        this.tagRepository = tagRepository;
    }

    public ArticleResponse createArticle(ArticleDTO articleDTO) {
        Article article = new Article();
        if (articleDTO.getThumbnailId()==null){
            article.setThumbnail(this.mediaFileRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("No thumbnail default")));
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

        return convertToResponse(articleRepository.save(article));
    }
    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
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
        return convertToResponse(articleRepository.save(article));
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    public ArticleResponse getArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        return convertToResponse(article);
    }
    public Page<ArticleResponse> getArticlesByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findByCategoryId(categoryId, pageable);
        return articlePage.map(this::convertToResponse);
    }

    public Page<ArticleResponse> getArticlesByStatus(ArticleStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Article> articlePage = articleRepository.findByStatus(status, pageable);
        return articlePage.map(this::convertToResponse);
    }

    @Transactional
    public void incrementViewCount(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
    }

    public Page<ArticleResponse> searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.searchArticles(keyword, pageable);
        return articlePage.map(this::convertToResponse);
    }
    public ArticleDetailResponse getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
        article.setViewCount(article.getViewCount()+1);
        this.articleRepository.save(article);
        List<Long> tagIds = article.getTags().stream().map(Tag::getId).toList();
        return ArticleDetailResponse.builder()
                .articleResponse(convertToResponse(article))
                .list(articleRepository.findRelatedArticles(article.getId(), article.getCategory().getId(), tagIds)
                        .stream()
                        .map(this::convertToResponse)
                        .collect(Collectors.toList()))
                .build();
    }
    public ArticleResponse moveToDraft(Long articleId){
        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find article with article ID"));
        article.setStatus(ArticleStatus.DRAFT);
        return convertToResponse(this.articleRepository.save(article));
    }
    public Page<ArticleResponse> getPublishedArticles(String keyword, String filter, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Article> articlePage = null;
        if (categoryId==0){
            if (filter.equalsIgnoreCase("all")){
                articlePage = articleRepository.findArticles(pageable);
            } else if (filter.equalsIgnoreCase("published")){
                articlePage = articleRepository.findPublishedArticles(pageable);
            } else articlePage = articleRepository.findDraftArticles(pageable);
            return articlePage.map(this::convertToResponse);
        }
        if (keyword.length()>0){
            return this.searchArticles(keyword, page, size);
        }
        if (filter.equalsIgnoreCase("all")){
            articlePage = articleRepository.findArticlesCategory(pageable, categoryId);
        } else if (filter.equalsIgnoreCase("published")){
            articlePage = articleRepository.findPublishedArticlesCategory(pageable, categoryId);
        } else articlePage = articleRepository.findDraftArticlesCategory(pageable, categoryId);
        return articlePage.map(this::convertToResponse);
    }
    public Page<ArticleResponse> getDraftArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<Article> articlePage = articleRepository.findDraftArticles(pageable);
        return articlePage.map(this::convertToResponse);
    }
    public ArticleResponse matchTagArticle(Long articleId,
                                            Long tagId){
        Tag tag = this.tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find tag"));
        Article article = this.articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find article"));
        Set<Tag> tags = article.getTags();
        tags.add(tag);
        article.setTags(tags);
        return this.convertToResponse(this.articleRepository.save(article));
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
        return ArticleResponse.builder()
                .id(article.getId())
                .slug(article.getSlug())
                .title(article.getTitle())
                .content(article.getContent())
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
