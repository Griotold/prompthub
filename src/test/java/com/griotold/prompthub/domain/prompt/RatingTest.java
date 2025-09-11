package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewFixture;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RatingTest {
    /**
     * 생성자
     * */
    @Test
    void Rating() {
        // when
        Rating rating1 = new Rating(0, 0);  // 빈 상태
        Rating rating2 = new Rating(15, 3); // 총 15점, 3개 리뷰
        Rating rating3 = new Rating(5, 1);  // 총 5점, 1개 리뷰

        // then
        assertThat(rating1.totalScore()).isEqualTo(0);
        assertThat(rating1.reviewsCount()).isEqualTo(0);

        assertThat(rating2.totalScore()).isEqualTo(15);
        assertThat(rating2.reviewsCount()).isEqualTo(3);

        assertThat(rating3.totalScore()).isEqualTo(5);
        assertThat(rating3.reviewsCount()).isEqualTo(1);
    }

    @Test
    void Rating_리뷰수가_음수이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Rating(10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 수는 0 이상이어야 합니다.");
    }

    @Test
    void Rating_총점이_음수이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Rating(-1, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("총점은 0 이상이어야 합니다.");
    }

    @Test
    void Rating_리뷰수가_0인데_총점이_0이_아니면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Rating(10, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰가 없으면 총점도 0이어야 합니다.");
    }

    /**
     * empty()
     * */
    @Test
    void empty() {
        // when
        Rating rating = Rating.empty();

        // then
        assertThat(rating.totalScore()).isEqualTo(0);
        assertThat(rating.reviewsCount()).isEqualTo(0);
    }

    /**
     * ofFirst()
     * */
    @Test
    void ofFirst() {
        // when
        Rating rating1 = Rating.ofFirst(1);
        Rating rating2 = Rating.ofFirst(3);
        Rating rating3 = Rating.ofFirst(5);

        // then
        assertThat(rating1.totalScore()).isEqualTo(1);
        assertThat(rating1.reviewsCount()).isEqualTo(1);

        assertThat(rating2.totalScore()).isEqualTo(3);
        assertThat(rating2.reviewsCount()).isEqualTo(1);

        assertThat(rating3.totalScore()).isEqualTo(5);
        assertThat(rating3.reviewsCount()).isEqualTo(1);
    }

    @Test
    void ofFirst_평점이_1미만이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> Rating.ofFirst(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    @Test
    void ofFirst_평점이_5초과이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> Rating.ofFirst(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    /**
     * add(int rating)
     * */
    @Test
    void add() {
        // given
        Rating emptyRating = new Rating(0, 0);
        Rating existingRating = new Rating(10, 2); // 총 10점, 2개 리뷰

        // when
        Rating result1 = emptyRating.add(5);
        Rating result2 = existingRating.add(3);
        Rating result3 = existingRating.add(1);

        // then
        assertThat(result1.totalScore()).isEqualTo(5);
        assertThat(result1.reviewsCount()).isEqualTo(1);

        assertThat(result2.totalScore()).isEqualTo(13);
        assertThat(result2.reviewsCount()).isEqualTo(3);

        assertThat(result3.totalScore()).isEqualTo(11);
        assertThat(result3.reviewsCount()).isEqualTo(3);
    }

    @Test
    void add_평점이_1미만이면_예외발생() {
        // given
        Rating rating = new Rating(10, 2);

        // when & then
        assertThatThrownBy(() -> rating.add(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    @Test
    void add_평점이_5초과이면_예외발생() {
        // given
        Rating rating = new Rating(10, 2);

        // when & then
        assertThatThrownBy(() -> rating.add(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    /**
     * remove(int rating)
     * */
    @Test
    void remove() {
        // given
        Rating rating1 = new Rating(5, 1);   // 5점 1개
        Rating rating2 = new Rating(13, 3);  // 총 13점, 3개 리뷰
        Rating rating3 = new Rating(8, 2);   // 총 8점, 2개 리뷰

        // when
        Rating result1 = rating1.remove(5);  // 마지막 리뷰 삭제
        Rating result2 = rating2.remove(4);  // 4점 리뷰 삭제
        Rating result3 = rating3.remove(3);  // 3점 리뷰 삭제

        // then
        assertThat(result1.totalScore()).isEqualTo(0);
        assertThat(result1.reviewsCount()).isEqualTo(0);

        assertThat(result2.totalScore()).isEqualTo(9);
        assertThat(result2.reviewsCount()).isEqualTo(2);

        assertThat(result3.totalScore()).isEqualTo(5);
        assertThat(result3.reviewsCount()).isEqualTo(1);
    }

    @Test
    void remove_평점이_1미만이면_예외발생() {
        // given
        Rating rating = new Rating(10, 2);

        // when & then
        assertThatThrownBy(() -> rating.remove(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    @Test
    void remove_평점이_5초과이면_예외발생() {
        // given
        Rating rating = new Rating(10, 2);

        // when & then
        assertThatThrownBy(() -> rating.remove(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다.");
    }

    @Test
    void remove_리뷰가_없는_상태에서_삭제하면_예외발생() {
        // given
        Rating emptyRating = new Rating(0, 0);

        // when & then
        assertThatThrownBy(() -> emptyRating.remove(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제할 리뷰가 없습니다.");
    }

    /**
     * add(Review)
     * */
    @Test
    void add_Review() {
        // given
        Rating emptyRating = new Rating(0, 0);
        Rating existingRating = new Rating(10, 2); // 총 10점, 2개 리뷰

        Review review1 = ReviewFixture.createReview(5, "훌륭합니다");
        Review review2 = ReviewFixture.createReview(3, "보통이에요");
        Review review3 = ReviewFixture.createReview(1, "별로네요");

        // when
        Rating result1 = emptyRating.add(review1);
        Rating result2 = existingRating.add(review2);
        Rating result3 = existingRating.add(review3);

        // then
        assertThat(result1.totalScore()).isEqualTo(5);
        assertThat(result1.reviewsCount()).isEqualTo(1);

        assertThat(result2.totalScore()).isEqualTo(13);
        assertThat(result2.reviewsCount()).isEqualTo(3);

        assertThat(result3.totalScore()).isEqualTo(11);
        assertThat(result3.reviewsCount()).isEqualTo(3);
    }

    /**
     * remove(Review)
     * */
    @Test
    void remove_Review() {
        // given
        Rating rating1 = new Rating(5, 1);   // 5점 1개
        Rating rating2 = new Rating(13, 3);  // 총 13점, 3개 리뷰
        Rating rating3 = new Rating(8, 2);   // 총 8점, 2개 리뷰

        Review review1 = ReviewFixture.createReview(5, "제거할 리뷰");
        Review review2 = ReviewFixture.createReview(4, "제거할 리뷰");
        Review review3 = ReviewFixture.createReview(3, "제거할 리뷰");

        // when
        Rating result1 = rating1.remove(review1);  // 마지막 리뷰 삭제
        Rating result2 = rating2.remove(review2);  // 4점 리뷰 삭제
        Rating result3 = rating3.remove(review3);  // 3점 리뷰 삭제

        // then
        assertThat(result1.totalScore()).isEqualTo(0);
        assertThat(result1.reviewsCount()).isEqualTo(0);

        assertThat(result2.totalScore()).isEqualTo(9);
        assertThat(result2.reviewsCount()).isEqualTo(2);

        assertThat(result3.totalScore()).isEqualTo(5);
        assertThat(result3.reviewsCount()).isEqualTo(1);
    }

    @Test
    void remove_Review_리뷰가_없는_상태에서_삭제하면_예외발생() {
        // given
        Rating emptyRating = new Rating(0, 0);
        Review review = ReviewFixture.createReview(3, "삭제할 리뷰");

        // when & then
        assertThatThrownBy(() -> emptyRating.remove(review))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("삭제할 리뷰가 없습니다.");
    }

    /**
     * getAverage()
     * */
    @Test
    void getAverage() {
        // given
        Rating emptyRating = new Rating(0, 0);      // 리뷰 없음
        Rating rating1 = new Rating(150, 30);       // 총 150점, 30개 리뷰 -> 평균 5.0
        Rating rating2 = new Rating(300, 75);       // 총 300점, 75개 리뷰 -> 평균 4.0
        Rating rating3 = new Rating(19, 5);         // 총 19점, 5개 리뷰 -> 평균 3.8
        Rating rating4 = new Rating(23, 10);        // 총 23점, 10개 리뷰 -> 평균 2.3

        // when & then
        assertThat(emptyRating.getAverage()).isEqualTo(0.0);    // 리뷰 없을 때
        assertThat(rating1.getAverage()).isEqualTo(5.0);        // 평균 5.0
        assertThat(rating2.getAverage()).isEqualTo(4.0);        // 평균 4.0
        assertThat(rating3.getAverage()).isEqualTo(3.8);        // 평균 3.8
        assertThat(rating4.getAverage()).isEqualTo(2.3);        // 평균 2.3
    }

    /**
     * hasReviews()
     * */
    @Test
    void hasReviews() {
        // given
        Rating emptyRating = new Rating(0, 0);      // 리뷰 없음
        Rating ratingWithReviews = new Rating(15, 3); // 리뷰 있음

        // when & then
        assertThat(emptyRating.hasReviews()).isFalse();     // 리뷰 없을 때
        assertThat(ratingWithReviews.hasReviews()).isTrue(); // 리뷰 있을 때
    }
}