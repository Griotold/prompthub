package com.griotold.prompthub.domain.prompt;

public record Rating(int totalScore, int reviewsCount) {

    public Rating add(int rating) {
        return new Rating(totalScore + rating * 10, reviewsCount + 1);
    }

    public Rating remove(int rating) {
        if (reviewsCount == 0) throw new IllegalStateException("삭제할 리뷰가 없습니다.");
        return new Rating(totalScore - rating * 10, reviewsCount - 1);
    }

    public double getAverage() {
        return reviewsCount == 0 ? 0 : totalScore / 10.0 / reviewsCount;
    }
}
