package com.example.news.services;

import com.example.news.dtos.CategoryDTO;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.Category;
import com.example.news.repositories.CategoryRepository;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.CategoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SlugGenerator slugGenerator;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, SlugGenerator slugGenerator) {
        this.categoryRepository = categoryRepository;
        this.slugGenerator = slugGenerator;
    }

    @Transactional
    public ApiResponse<CategoryResponse> createCategory(CategoryDTO category) {
        Category entity = new Category();
        entity.setSlug(slugGenerator.generateSlug(category.getName()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        return ApiResponse.created(this.mapToResponse(categoryRepository.save(entity)), "Category created successfully");
    }

    public ApiResponse<CategoryResponse> getCategoryById(Long id) {
        return ApiResponse.success(this.mapToResponse(categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Can not find with ID"))));
    }

    public ApiResponse<CategoryResponse> getCategoryBySlug(String slug) {
        return ApiResponse.success(this.mapToResponse(categoryRepository.findBySlug(slug).orElseThrow(
                () -> new ResourceNotFoundException("Can not find with ID"))));
    }

    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.success(categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @Transactional
    public ApiResponse<CategoryResponse> updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find category"));
        if (categoryDTO.getDescription()!=null){
            category.setDescription(categoryDTO.getDescription());
        }
        if (categoryDTO.getName()!=null){
            category.setName(categoryDTO.getName());
            category.setSlug(slugGenerator.generateSlug(category.getName()));
        }
        return ApiResponse.success(this.mapToResponse(categoryRepository.save(category)), "Category updated successfully");
    }

    @Transactional
    public ApiResponse<Void> deleteCategory(Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not delete with id"));
        categoryRepository.deleteById(id);
        return ApiResponse.success(null, "Category deleted successfully");
    }

    public ApiResponse<List<Category>> getCategoriesByIds(List<Long> ids) {
        return ApiResponse.success(categoryRepository.findAllById(ids));
    }

    private CategoryResponse mapToResponse(Category category){
        return CategoryResponse.builder()
                .createdAt(category.getCreatedAt())
                .description(category.getDescription())
                .name(category.getName())
                .slug(category.getSlug())
                .id(category.getId())
                .build();
    }
}
