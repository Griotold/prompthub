package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromptTest {

    Prompt prompt;
    Member member;
    Category category;

    @BeforeEach
    void setUp() {
        prompt = PromptFixture.createPrompt("테스트 제목", "테스트 내용", "테스트 설명");
        member = prompt.getMember();
        category = prompt.getCategory();
    }

    @Test
    void register() {
        assertThat(prompt.getTitle()).isEqualTo("테스트 제목");
        assertThat(prompt.getContent()).isEqualTo("테스트 내용");
        assertThat(prompt.getDescription()).isEqualTo("테스트 설명");
        assertThat(prompt.getMember()).isNotNull();
        assertThat(prompt.getCategory()).isNotNull();
        assertThat(prompt.getViewsCount()).isEqualTo(0);
        assertThat(prompt.getLikesCount()).isEqualTo(0);
        assertThat(prompt.getIsPublic()).isTrue();
        // Rating 관련
        assertThat(prompt.getAverageRating()).isEqualTo(0.0);
        assertThat(prompt.hasReviews()).isFalse();
        assertThat(prompt.getReviewsCount()).isEqualTo(0);
    }

    @Test
    void makePrivate() {
        assertThat(prompt.getIsPublic()).isTrue();

        prompt.makePrivate();

        assertThat(prompt.getIsPublic()).isFalse();
    }

    @Test
    void makePublic() {
        prompt.makePrivate();
        assertThat(prompt.getIsPublic()).isFalse();

        prompt.makePublic();

        assertThat(prompt.getIsPublic()).isTrue();
    }

    @Test
    void update() {
        PromptUpdateRequest updateRequest = PromptFixture.createPromptUpdateRequest("수정된 제목", "수정된 내용", "수정된 설명");

        prompt.update(updateRequest);

        assertThat(prompt.getTitle()).isEqualTo("수정된 제목");
        assertThat(prompt.getContent()).isEqualTo("수정된 내용");
        assertThat(prompt.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void increaseViewCount() {
        assertThat(prompt.getViewsCount()).isEqualTo(0);

        prompt.increaseViewCount();

        assertThat(prompt.getViewsCount()).isEqualTo(1);

        prompt.increaseViewCount();

        assertThat(prompt.getViewsCount()).isEqualTo(2);
    }

    @Test
    void isOwnedBy() {
        assertThat(prompt.isOwnedBy(member)).isTrue();

        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );

        assertThat(prompt.isOwnedBy(anotherMember)).isFalse();
    }

    @Test
    void increaseLikeCount() {
        assertThat(prompt.getLikesCount()).isEqualTo(0);

        prompt.increaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(1);
    }

    @Test
    void decreaseLikeCount() {
        prompt.increaseLikeCount();
        assertThat(prompt.getLikesCount()).isEqualTo(1);

        prompt.decreaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(0);
    }

    @Test
    void decreaseLikeCount_0이하로_내려가지_않음() {
        assertThat(prompt.getLikesCount()).isEqualTo(0);

        prompt.decreaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(0);
    }

    /**
     * Rating 관련
     * */
    @Test
    void addRating() {
        // given
        assertThat(prompt.getAverageRating()).isEqualTo(0.0);
        assertThat(prompt.hasReviews()).isFalse();

        Review review1 = ReviewFixture.createReview(prompt, member, 5);
        Review review2 = ReviewFixture.createReview(prompt, member, 3);

        // when
        prompt.addRating(review1);

        // then
        assertThat(prompt.getAverageRating()).isEqualTo(5.0);
        assertThat(prompt.getReviewsCount()).isEqualTo(1);
        assertThat(prompt.hasReviews()).isTrue();

        // when - 두 번째 리뷰 추가
        prompt.addRating(review2);

        // then
        assertThat(prompt.getAverageRating()).isEqualTo(4.0); // (5+3)/2 = 4.0
        assertThat(prompt.getReviewsCount()).isEqualTo(2);
        assertThat(prompt.hasReviews()).isTrue();
    }

    @Test
    void removeRating() {
        // given
        Review review1 = ReviewFixture.createReview(prompt, member, 5);
        Review review2 = ReviewFixture.createReview(prompt, member, 3);
        Review review3 = ReviewFixture.createReview(prompt, member, 4);

        prompt.addRating(review1);
        prompt.addRating(review2);
        prompt.addRating(review3);

        assertThat(prompt.getAverageRating()).isEqualTo(4.0); // (5+3+4)/3 = 4.0
        assertThat(prompt.getReviewsCount()).isEqualTo(3);
        assertThat(prompt.hasReviews()).isTrue();

        // when - 3점 리뷰 제거
        prompt.removeRating(review2);

        // then
        assertThat(prompt.getAverageRating()).isEqualTo(4.5); // (5+4)/2 = 4.5
        assertThat(prompt.getReviewsCount()).isEqualTo(2);
        assertThat(prompt.hasReviews()).isTrue();

        // when - 리뷰 모두 제거
        prompt.removeRating(review1);
        prompt.removeRating(review3);

        // then
        assertThat(prompt.getAverageRating()).isEqualTo(0.0);
        assertThat(prompt.getReviewsCount()).isEqualTo(0);
        assertThat(prompt.hasReviews()).isFalse();
    }

    @Test
    void getAverageRating() {
        // given
        assertThat(prompt.getAverageRating()).isEqualTo(0.0); // 초기 상태

        Review review1 = ReviewFixture.createReview(prompt, member, 5);
        Review review2 = ReviewFixture.createReview(prompt, member, 3);
        Review review3 = ReviewFixture.createReview(prompt, member, 4);
        Review review4 = ReviewFixture.createReview(prompt, member, 2);

        // when & then
        prompt.addRating(review1);
        assertThat(prompt.getAverageRating()).isEqualTo(5.0); // 5/1 = 5.0

        prompt.addRating(review2);
        assertThat(prompt.getAverageRating()).isEqualTo(4.0); // (5+3)/2 = 4.0

        prompt.addRating(review3);
        assertThat(prompt.getAverageRating()).isEqualTo(4.0); // (5+3+4)/3 = 4.0

        prompt.addRating(review4);
        assertThat(prompt.getAverageRating()).isEqualTo(3.5); // (5+3+4+2)/4 = 3.5
    }

    @Test
    void hasReviews() {
        // given & then - 초기 상태
        assertThat(prompt.hasReviews()).isFalse();

        Review review = ReviewFixture.createReview(prompt, member, 4);

        // when - 리뷰 추가
        prompt.addRating(review);

        // then
        assertThat(prompt.hasReviews()).isTrue();

        // when - 리뷰 제거
        prompt.removeRating(review);

        // then
        assertThat(prompt.hasReviews()).isFalse();
    }

    /**
     * Price 관련
     */
    @Test
    void register_초기_가격_상태() {
        // then - 새 프롬프트는 기본적으로 무료
        assertThat(prompt.getPriceAmount()).isEqualTo(0);
        assertThat(prompt.isFree()).isTrue();
        assertThat(prompt.isPremium()).isFalse();
        assertThat(prompt.getSalesCount()).isEqualTo(0);
    }

    @Test
    void changePrice() {
        // given
        assertThat(prompt.isFree()).isTrue();

        // when - 무료에서 유료로 변경
        prompt.changePrice(5000);

        // then
        assertThat(prompt.getPriceAmount()).isEqualTo(5000);
        assertThat(prompt.isFree()).isFalse();
        assertThat(prompt.isPremium()).isTrue();
        assertThat(prompt.getSalesCount()).isEqualTo(0); // 판매 횟수는 유지

        // when - 다른 가격으로 변경
        prompt.changePrice(3000);

        // then
        assertThat(prompt.getPriceAmount()).isEqualTo(3000);
        assertThat(prompt.isPremium()).isTrue();
    }

    @Test
    void makeFree() {
        // given - 유료 프롬프트로 만들고 판매 기록 추가
        prompt.changePrice(10000);
        prompt.increaseSalesCount();
        prompt.increaseSalesCount();

        assertThat(prompt.isPremium()).isTrue();
        assertThat(prompt.getSalesCount()).isEqualTo(2);

        // when
        prompt.makeFree();

        // then
        assertThat(prompt.getPriceAmount()).isEqualTo(0);
        assertThat(prompt.isFree()).isTrue();
        assertThat(prompt.isPremium()).isFalse();
        assertThat(prompt.getSalesCount()).isEqualTo(2); // 과거 판매 기록 유지!
    }

    @Test
    void increaseSalesCount() {
        // given
        prompt.changePrice(5000); // 유료로 변경
        assertThat(prompt.getSalesCount()).isEqualTo(0);

        // when & then
        prompt.increaseSalesCount();
        assertThat(prompt.getSalesCount()).isEqualTo(1);

        prompt.increaseSalesCount();
        assertThat(prompt.getSalesCount()).isEqualTo(2);

        prompt.increaseSalesCount();
        assertThat(prompt.getSalesCount()).isEqualTo(3);
    }

    @Test
    void getSellerRevenue() {
        // given
        prompt.changePrice(1000);

        // when & then - 판매 없을 때
        assertThat(prompt.getSellerRevenue()).isEqualTo(0);

        // when & then - 1회 판매
        prompt.increaseSalesCount();
        assertThat(prompt.getSellerRevenue()).isEqualTo(800); // 1000 * 1 * 0.8

        // when & then - 3회 판매
        prompt.increaseSalesCount();
        prompt.increaseSalesCount();
        assertThat(prompt.getSellerRevenue()).isEqualTo(2400); // 1000 * 3 * 0.8
    }

    @Test
    void getPlatformCommission() {
        // given
        prompt.changePrice(5000);

        // when & then - 판매 없을 때
        assertThat(prompt.getPlatformCommission()).isEqualTo(0);

        // when & then - 2회 판매
        prompt.increaseSalesCount();
        prompt.increaseSalesCount();
        assertThat(prompt.getPlatformCommission()).isEqualTo(2000); // 5000 * 2 * 0.2
    }

    @Test
    void isFree_isPremium_반대_관계() {
        // given & then - 초기 무료 상태
        assertThat(prompt.isFree()).isTrue();
        assertThat(prompt.isPremium()).isFalse();

        // when - 유료로 변경
        prompt.changePrice(3000);

        // then
        assertThat(prompt.isFree()).isFalse();
        assertThat(prompt.isPremium()).isTrue();

        // when - 다시 무료로 변경
        prompt.makeFree();

        // then
        assertThat(prompt.isFree()).isTrue();
        assertThat(prompt.isPremium()).isFalse();
    }
}