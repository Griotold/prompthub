package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.review.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Rating(
        @Column(name = "total_score", nullable = false)
        Integer totalScore,

        @Column(name = "reviews_count", nullable = false)
        Integer reviewsCount
) {

    public Rating {
        if (reviewsCount < 0) {
            throw new IllegalArgumentException("리뷰 수는 0 이상이어야 합니다.");
        }
        if (totalScore < 0) {
            throw new IllegalArgumentException("총점은 0 이상이어야 합니다.");
        }
        if (reviewsCount == 0 && totalScore != 0) {
            throw new IllegalArgumentException("리뷰가 없으면 총점도 0이어야 합니다.");
        }
    }

    // 새 프롬프트 생성할 때, 처음엔 평점이 없으니까
    public static Rating empty() {
        return new Rating(0, 0);
    }

    // 첫 번째 리뷰 등록시, empty().add(rating) 과 같은 결과지만 의도가 더 명확
    public static Rating ofFirst(int rating) {
        validateRating(rating);
        return new Rating(rating, 1);
    }

    /**
     * 리뷰 엔티티를 받아서 평점을 추가합니다.
     * 실제 비즈니스 로직에서 Review 엔티티가 있을 때 사용하세요.
     * @param review 추가할 리뷰
     * @return 새로운 Rating 인스턴스
     */
    public Rating add(Review review) {
        validateRating(review.getRating());
        return new Rating(totalScore + review.getRating(), reviewsCount + 1);
    }

    /**
     * 평점 값으로 직접 추가합니다.
     * 테스트나 계산 로직에서 사용하세요.
     * @param rating 1-5 범위의 평점값
     * @return 새로운 Rating 인스턴스
     */
    public Rating add(int rating) {
        validateRating(rating);
        return new Rating(totalScore + rating, reviewsCount + 1);
    }

    /**
     * 리뷰 엔티티를 받아서 평점을 제거합니다.
     * 실제 비즈니스 로직에서 Review 엔티티가 있을 때 사용하세요.
     * @param review 제거할 리뷰
     * @return 새로운 Rating 인스턴스
     * @throws IllegalArgumentException 평점이 1-5 범위를 벗어날 때
     * @throws IllegalStateException 제거할 리뷰가 없을 때
     */
    public Rating remove(Review review) {
        validateRating(review.getRating());
        if (reviewsCount == 0) {
            throw new IllegalStateException("삭제할 리뷰가 없습니다.");
        }
        return new Rating(totalScore - review.getRating(), reviewsCount - 1);
    }

    /**
     * 평점 값으로 직접 제거합니다.
     * 테스트나 계산 로직에서 사용하세요.
     * @param rating 1-5 범위의 평점값
     * @return 새로운 Rating 인스턴스
     * @throws IllegalArgumentException 평점이 1-5 범위를 벗어날 때
     * @throws IllegalStateException 제거할 리뷰가 없을 때
     */
    public Rating remove(int rating) {
        validateRating(rating);
        if (reviewsCount == 0) {
            throw new IllegalStateException("삭제할 리뷰가 없습니다.");
        }
        return new Rating(totalScore - rating, reviewsCount - 1);
    }

    public double getAverage() {
        return reviewsCount == 0 ? 0.0 : (double) totalScore / reviewsCount;
    }

    // null 체크 대신 명확한 비즈니스 로직 표현
    public boolean hasReviews() {
        return reviewsCount > 0;
    }

    private static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 범위여야 합니다.");
        }
    }
}
