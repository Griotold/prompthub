package com.griotold.prompthub.adapter.webapi.review;

import com.griotold.prompthub.domain.review.Review;
import org.springframework.data.domain.Slice;

import java.util.List;

public record ReviewListResponse(
        List<ReviewDetailResponse> reviews,
        boolean hasNext
) {
    public static ReviewListResponse from(Slice<Review> reviewSlice) {
        List<ReviewDetailResponse> reviews = reviewSlice.getContent().stream()
                .map(ReviewDetailResponse::of)
                .toList();
        return new ReviewListResponse(reviews, reviewSlice.hasNext());
    }
}
