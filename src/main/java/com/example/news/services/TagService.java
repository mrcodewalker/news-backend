package com.example.news.services;

import com.example.news.dtos.TagDTO;
import com.example.news.handler.ResourceNotFoundException;
import com.example.news.inits.SlugGenerator;
import com.example.news.models.Tag;
import com.example.news.repositories.TagRepository;
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
    public TagResponse createTag(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setSlug(slugGenerator.generateSlug(tagDTO.getName()));
        tag.setName(tagDTO.getName());
        tag.setCreatedAt(LocalDateTime.now());
        return this.mapToResponse(tagRepository.save(tag));
    }

    public TagResponse getTagById(Long id) {
        return this.mapToResponse(tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find with id")));
    }

    public TagResponse getTagBySlug(String slug) {
        return this.mapToResponse(tagRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Can not find with slug")));
    }

    public List<TagResponse> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TagResponse updateTag(Long id, TagDTO tagDTO) {
        Tag tag = this.tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not update right now!"));
        if (tagDTO.getName()!=null){
            tag.setSlug(slugGenerator.generateSlug(tagDTO.getName()));
            tag.setName(tagDTO.getName());
        }
        return this.mapToResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = this.tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Can not update right now!"));
        tagRepository.deleteById(id);
    }

    public List<Tag> getTagsByIds(List<Long> ids) {
        return tagRepository.findAllById(ids);
    }
    private TagResponse mapToResponse(Tag tag){
        return TagResponse.builder()
                .name(tag.getName())
                .slug(tag.getSlug())
                .id(tag.getId())
                .build();
    }
}
