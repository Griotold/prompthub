package com.griotold.prompthub.adapter.webapi.review;

import com.griotold.prompthub.adapter.util.DateFormatUtils;
import com.griotold.prompthub.domain.review.Review;

public record ReviewDetailResponse(
        Long id,
        Integer rating,
        String content,
        String authorNickname,
        String createdAt,
        String updatedAt
) {
    public static ReviewDetailResponse of(Review review) {
        return new ReviewDetailResponse(
                review.getId(),
                review.getRating(),
                review.getContent(),
                review.getMember().getNickname(),
                DateFormatUtils.formatReviewDate(review.getCreatedAt()),
                DateFormatUtils.formatReviewDate(review.getUpdatedAt())
        );
    }
}
