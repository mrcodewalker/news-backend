package com.example.news.services;

import com.example.news.dtos.SubCategoryDTO;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.models.Category;
import com.example.news.models.SubCategory;
import com.example.news.repositories.CategoryRepository;
import com.example.news.repositories.SubCategoryRepository;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.SubCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SubCategoryService {
    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    public ApiResponse<SubCategoryResponse> createSubCategory(SubCategoryDTO dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (subCategoryRepository.existsByNameAndCategoryId(dto.getName(), dto.getCategoryId())) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST, "Subcategory with this name already exists in the category");
        }

        SubCategory subCategory = new SubCategory();
        subCategory.setCategory(category);
        subCategory.setName(dto.getName());
        subCategory.setSlug(generateSlug(dto.getName()));

        SubCategory savedSubCategory = subCategoryRepository.save(subCategory);
        return ApiResponse.created(convertToResponse(savedSubCategory), "Subcategory created successfully");
    }

    public ApiResponse<SubCategoryResponse> updateSubCategory(Long id, SubCategoryDTO dto) {
        SubCategory subCategory = subCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        if (!subCategory.getCategory().getId().equals(dto.getCategoryId())) {
            Category newCategory = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            subCategory.setCategory(newCategory);
        }

        subCategory.setName(dto.getName());
        subCategory.setSlug(generateSlug(dto.getName()));

        SubCategory updatedSubCategory = subCategoryRepository.save(subCategory);
        return ApiResponse.success(convertToResponse(updatedSubCategory), "Subcategory updated successfully");
    }

    public ApiResponse<List<SubCategoryResponse>> getSubCategoriesByCategory(Long categoryId) {
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryId(categoryId);
        return ApiResponse.success(subCategories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
    }

    private SubCategoryResponse convertToResponse(SubCategory subCategory) {
        return modelMapper.map(subCategory, SubCategoryResponse.class);
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove all non-alphanumeric characters except spaces and hyphens
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-");          // Replace multiple hyphens with single hyphen

        String baseSlug = slug;
        int counter = 1;
        while (subCategoryRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }
}
