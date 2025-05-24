package com.example.news.services;

import com.example.news.dtos.TagDTO;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.Tag;
import com.example.news.repositories.TagRepository;
import com.example.news.responses.ApiResponse;
import com.example.news.responses.TagResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final SlugGenerator slugGenerator;

    @Autowired
    public TagService(TagRepository tagRepository, SlugGenerator slugGenerator) {
        this.tagRepository = tagRepository;
        this.slugGenerator = slugGenerator;
    }

    @Transactional
    public ApiResponse<TagResponse> createTag(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setSlug(slugGenerator.generateSlug(tagDTO.getName()));
        tag.setName(tagDTO.getName());
        tag.setCreatedAt(LocalDateTime.now());
        return ApiResponse.created(this.mapToResponse(tagRepository.save(tag)), "Tag created successfully");
    }

    public ApiResponse<TagResponse> getTagById(Long id) {
        return ApiResponse.success(this.mapToResponse(tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find with id"))));
    }

    public ApiResponse<TagResponse> getTagBySlug(String slug) {
        return ApiResponse.success(this.mapToResponse(tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find with slug"))));
    }

    public ApiResponse<List<TagResponse>> getAllTags() {
        return ApiResponse.success(tagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    @Transactional
    public ApiResponse<TagResponse> updateTag(Long id, TagDTO tagDTO) {
        Tag tag = this.tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not update right now!"));
        if (tagDTO.getName()!=null){
            tag.setSlug(slugGenerator.generateSlug(tagDTO.getName()));
            tag.setName(tagDTO.getName());
        }
        return ApiResponse.success(this.mapToResponse(tagRepository.save(tag)), "Tag updated successfully");
    }

    @Transactional
    public ApiResponse<Void> deleteTag(Long id) {
        Tag tag = this.tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not update right now!"));
        tagRepository.deleteById(id);
        return ApiResponse.success(null, "Tag deleted successfully");
    }

    public ApiResponse<List<Tag>> getTagsByIds(List<Long> ids) {
        return ApiResponse.success(tagRepository.findAllById(ids));
    }

    private TagResponse mapToResponse(Tag tag){
        return TagResponse.builder()
                .name(tag.getName())
                .slug(tag.getSlug())
                .id(tag.getId())
                .build();
    }
}
