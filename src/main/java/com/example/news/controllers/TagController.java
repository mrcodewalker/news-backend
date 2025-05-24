package com.example.news.controllers;

import com.example.news.dtos.CategoryDTO;
import com.example.news.dtos.TagDTO;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.TagResponse;
import com.example.news.services.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tag")
public class TagController {
    private final TagService tagService;
    @Autowired
    public TagController(TagService tagService){
        this.tagService = tagService;
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @RequestBody TagDTO tagDTO){
        return ResponseEntity.ok(this.tagService.createTag(tagDTO));
    }
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TagResponse>>> filterList(){
        return ResponseEntity.ok(this.tagService.getAllTags());
    }
    @GetMapping("/view/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(this.tagService.getTagById(id));
    }
    @PostMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateEntity(
            @PathVariable("id") Long id,
            @RequestBody TagDTO tagDTO){
        return ResponseEntity.ok(this.tagService.updateTag(id, tagDTO));
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEntity(
            @PathVariable("id") Long id
    ){
        return ResponseEntity.ok(this.tagService.deleteTag(id));
    }
}
