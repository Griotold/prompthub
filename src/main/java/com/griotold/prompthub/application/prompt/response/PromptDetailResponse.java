package com.griotold.prompthub.application.prompt.response;

import com.griotold.prompthub.adapter.webapi.category.CategoryInfoResponse;
import com.griotold.prompthub.application.tag.response.TagResponse;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.tag.Tag;

import java.time.LocalDateTime;
import java.util.List;

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
        List<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PromptDetailResponse of(Prompt prompt, boolean isLiked) {
        return of(prompt, isLiked, List.of());
    }

    public static PromptDetailResponse of(Prompt prompt, boolean isLiked, List<Tag> tags) {
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
                tags.stream().map(TagResponse::of).toList(),
                prompt.getCreatedAt(),
                prompt.getUpdatedAt()
        );
    }
}
