package com.griotold.prompthub.adapter.webapi.prompt;

import com.griotold.prompthub.domain.prompt.Prompt;

import java.time.LocalDateTime;

public record PromptListResponse(
        Long id,
        String title,
        String description,
        String categoryName,
        String authorNickname,
        Integer viewsCount,
        Integer likesCount,
        LocalDateTime createdAt
) {
    public static PromptListResponse of(Prompt prompt) {
        return new PromptListResponse(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getDescription(),
                prompt.getCategory().getName(),
                prompt.getMember().getNickname(),
                prompt.getViewsCount(),
                prompt.getLikesCount(),
                prompt.getCreatedAt()
        );
    }
}
