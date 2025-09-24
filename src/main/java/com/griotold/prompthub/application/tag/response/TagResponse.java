package com.griotold.prompthub.application.tag.response;

import com.griotold.prompthub.domain.tag.Tag;

public record TagResponse(
        Long id,
        String name
) {
    public static TagResponse of(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }
}
