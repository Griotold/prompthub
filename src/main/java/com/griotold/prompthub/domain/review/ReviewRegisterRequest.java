package com.griotold.prompthub.domain.review;

import jakarta.validation.constraints.*;

public record ReviewRegisterRequest(
        @NotNull(message = "평점은 필수입니다")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다")
        Integer rating,

        @NotBlank(message = "리뷰 내용은 필수입니다")
        @Size(max = 500, message = "리뷰 내용은 500자 이하여야 합니다")
        String content
) {
}
