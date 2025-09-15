package com.griotold.prompthub.domain.tag;

import java.util.List;

public class TagNames {
    private final List<String> names;

    private TagNames(List<String> names) {
        this.names = List.copyOf(names);
    }

    public static TagNames of(List<String> names) {
        return new TagNames(names);
    }

    public static TagNames ofDistinct(List<String> names) {
        return new TagNames(names.stream().distinct().toList());
    }

    public TagNames distinct() {
        return new TagNames(names.stream().distinct().toList());
    }

    public List<String> toList() {
        return List.copyOf(names);
    }

    public TagNames filterExisting(TagNames existingNames) {
        List<String> filteredNames = this.names.stream()
                .filter(name -> !existingNames.names.contains(name))
                .toList();
        return new TagNames(filteredNames);
    }

    public List<Tag> createTags() {
        return names.stream()
                .map(Tag::create)
                .toList();
    }
}
