package com.griotold.prompthub.domain.tag;

import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptTag;

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

    /**
     * 특정 프롬프트와의 PromptTag 연결 생성
     * @param prompt 연결할 프롬프트
     * @return PromptTag 리스트
     */
    public List<PromptTag> createLinksTo(Prompt prompt) {
        return tags.stream()
                .map(tag -> PromptTag.link(prompt, tag))
                .toList();
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public List<Tag> toList() {
        return List.copyOf(tags);
    }
}