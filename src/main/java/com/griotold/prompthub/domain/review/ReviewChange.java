package com.griotold.prompthub.domain.review;

import java.util.Objects;

public record ReviewChange(
        Integer oldRating,
        Integer newRating,
        String oldContent,
        String newContent
) {
    /**
     * 평점이 변경되었는지 확인
     */
    public boolean isRatingChanged() {
        return !Objects.equals(oldRating, newRating);
    }

    /**
     * 내용이 변경되었는지 확인
     */
    public boolean isContentChanged() {
        return !Objects.equals(oldContent, newContent);
    }

    /**
     * 평점 또는 내용 중 하나라도 변경되었는지 확인
     */
    public boolean hasAnyChange() {
        return isRatingChanged() || isContentChanged();
    }
}
