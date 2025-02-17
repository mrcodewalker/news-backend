package com.example.news.services;

import com.example.news.dtos.CategoryDTO;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.Category;
import com.example.news.repositories.CategoryRepository;
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
    public CategoryResponse createCategory(CategoryDTO category) {
        Category entity = new Category();
        entity.setSlug(slugGenerator.generateSlug(category.getName()));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setName(category.getName());
        entity.setDescription(category.getDescription());
        return this.mapToResponse(categoryRepository.save(entity));
    }

    public CategoryResponse getCategoryById(Long id) {
        return this.mapToResponse(categoryRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Can not find with ID")));
    }

    public CategoryResponse getCategoryBySlug(String slug) {
        return this.mapToResponse(categoryRepository.findBySlug(slug).orElseThrow(
                () -> new ResourceNotFoundException("Can not find with ID")));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = this.categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find category"));
        if (categoryDTO.getDescription()!=null){
            category.setDescription(categoryDTO.getDescription());
        }
        if (categoryDTO.getName()!=null){
            category.setName(categoryDTO.getName());
            category.setSlug(slugGenerator.generateSlug(category.getName()));
        }
        return this.mapToResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Can not delete with id"));
        categoryRepository.deleteById(id);
    }

    public List<Category> getCategoriesByIds(List<Long> ids) {
        return categoryRepository.findAllById(ids);
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
