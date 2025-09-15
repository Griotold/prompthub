package com.griotold.prompthub.domain.tag;

import java.util.ArrayList;
import java.util.List;

public class Tags {
    private final List<Tag> tags;

    private Tags(List<Tag> tags) {
        this.tags = List.copyOf(tags);
    }

    public static Tags of(List<Tag> tags) {
        return new Tags(tags);
    }

    public TagNames extractNames() {
        List<String> tagNames = tags.stream()
                .map(Tag::getName)
                .toList();
        return TagNames.of(tagNames);
    }

    public Tags combine(Tags other) {
        List<Tag> combinedTags = new ArrayList<>(this.tags);
        combinedTags.addAll(other.tags);
        return new Tags(combinedTags);
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public List<Tag> toList() {
        return List.copyOf(tags);
    }
}