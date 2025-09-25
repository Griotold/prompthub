package com.griotold.prompthub.application.prompt.response;

import com.griotold.prompthub.domain.prompt.Prompt;

import java.time.LocalDateTime;
// todo: List<TagResponse> -> Prompt 엔티티에 List<PromptTag> 추가 필요
public record PromptListResponse(
        Long id,
        String title,
        String description,
        String categoryName,
        String authorNickname,
        Integer viewsCount,
        Integer likesCount,
        Double averageRating,        // 🆕 리뷰 정보 (엔티티에서 바로 가져올 수 있음)
        Integer reviewsCount,        // 🆕 리뷰 정보 (엔티티에서 바로 가져올 수 있음)
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
                prompt.getAverageRating(),      // 🆕 리뷰 정보
                prompt.getReviewsCount(),       // 🆕 리뷰 정보
                prompt.getCreatedAt()
        );
    }

}