package com.example.news.inits;

import com.github.slugify.Slugify;
import org.springframework.stereotype.Component;

@Component
public class SlugGenerator {

    private final Slugify slugify;

    public SlugGenerator() {
        this.slugify = Slugify.builder()
                .lowerCase(true)
                .build();
    }

    public String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        return slugify.slugify(input);
    }
}
