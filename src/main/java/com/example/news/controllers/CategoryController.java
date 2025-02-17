package com.example.news.controllers;

import com.example.news.dtos.CategoryDTO;
import com.example.news.models.Category;
import com.example.news.services.CategoryService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/category")
public class CategoryController {
    private final CategoryService categoryService;
    @Autowired
    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createCategory(
            @RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(this.categoryService.createCategory(categoryDTO));
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<?> getCategoryById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(this.categoryService.getCategoryById(id));
    }
    @GetMapping("/filter")
    public ResponseEntity<?> filterList(){
        return ResponseEntity.ok(this.categoryService.getAllCategories());
    }
    @PostMapping("/update/{id}")
    public ResponseEntity<?> updateEntity(
            @PathVariable("id") Long id,
            @RequestBody CategoryDTO categoryDTO){
        return ResponseEntity.ok(this.categoryService.updateCategory(id, categoryDTO));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntity(
            @PathVariable("id") Long id
    ){
        this.categoryService.deleteCategory(id);
        return ResponseEntity.ok(HttpStatus.ACCEPTED);
    }
}
