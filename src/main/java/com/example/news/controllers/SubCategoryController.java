package com.example.news.controllers;

import com.example.news.dtos.SubCategoryDTO;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.SubCategoryResponse;
import com.example.news.services.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sub_category")
@RequiredArgsConstructor
public class SubCategoryController {
    private final SubCategoryService subCategoryService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> createSubCategory(@RequestBody SubCategoryDTO subCategoryDTO){
        return ResponseEntity.ok(this.subCategoryService.createSubCategory(subCategoryDTO));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<SubCategoryResponse>> updateCategory(
            @PathVariable("id") Long id,
            @RequestBody SubCategoryDTO subCategoryDTO){
        return ResponseEntity.ok(this.subCategoryService.updateSubCategory(id, subCategoryDTO));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<SubCategoryResponse>>> filterList(@RequestParam("categoryId") Long categoryId){
        return ResponseEntity.ok(this.subCategoryService.getSubCategoriesByCategory(categoryId));
    }
}
