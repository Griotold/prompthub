package com.griotold.prompthub.adapter.webapi.prompt;

import com.griotold.prompthub.adapter.webapi.category.CategoryInfoResponse;
import com.griotold.prompthub.domain.prompt.Prompt;

import java.time.LocalDateTime;

public record PromptDetailResponse(
        Long id,
        String title,
        String content,
        String description,
        CategoryInfoResponse category,
        String authorNickname,
        Integer viewsCount,
        Integer likesCount,
        boolean isLiked,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PromptDetailResponse of(Prompt prompt, boolean isLiked) {
        return new PromptDetailResponse(
                prompt.getId(),
                prompt.getTitle(),
                prompt.getContent(),
                prompt.getDescription(),
                CategoryInfoResponse.of(prompt.getCategory()),
                prompt.getMember().getNickname(),
                prompt.getViewsCount(),
                prompt.getLikesCount(),
                isLiked,
                prompt.getIsPublic(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }
}
