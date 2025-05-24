package com.example.news.controllers;

import com.example.news.dtos.CategoryDTO;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.CategoryResponse;
import com.example.news.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {
    private final CategoryService categoryService;
    
    @Autowired
    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(this.categoryService.createCategory(categoryDTO));
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable("id") Long id){
        return ResponseEntity.ok(this.categoryService.getCategoryById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> filterList(){
        return ResponseEntity.ok(this.categoryService.getAllCategories());
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateEntity(
            @PathVariable("id") Long id,
            @RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(this.categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(@PathVariable("id") Long id){
        return ResponseEntity.ok(this.categoryService.deleteCategory(id));
    }
}
