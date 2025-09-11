package com.griotold.prompthub.domain.review;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ReviewTest {

    Review review;
    Prompt prompt;
    Member member;

    @BeforeEach
    void setUp() {
        review = ReviewFixture.createReview();
        prompt = PromptFixture.createPrompt();

        MemberRegisterRequest memberRegisterRequest = MemberFixture.createMemberRegisterRequest();
        PasswordEncoder passwordEncoder = MemberFixture.createPasswordEncoder();
        member = Member.register(memberRegisterRequest, passwordEncoder);
    }

    @Test
    void register() {
        // given
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(4, "좋은 프롬프트입니다");

        // when
        Review createdReview = Review.register(request, prompt, member);

        // then
        assertThat(createdReview).isNotNull();
        assertThat(createdReview.getPrompt()).isEqualTo(prompt);
        assertThat(createdReview.getMember()).isEqualTo(member);
        assertThat(createdReview.getRating()).isEqualTo(4);
        assertThat(createdReview.getContent()).isEqualTo("좋은 프롬프트입니다");
    }

    @Test
    void register_프롬프트가_null() {
        // given
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest();

        // when & then
        assertThatThrownBy(() -> Review.register(request, null, member))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void register_멤버가_null() {
        // given
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest();

        // when & then
        assertThatThrownBy(() -> Review.register(request, prompt, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("리뷰를 수정할 수 있다")
    void update() {
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정말 유용한 프롬프트입니다. 많은 도움이 되었어요!");
        // given
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(3, "수정된 리뷰 내용입니다");

        // when
        review.update(updateRequest);

        // then
        assertThat(review.getRating()).isEqualTo(3);
        assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용입니다");
    }

    @Test
    @DisplayName("리뷰 수정 시 평점이 null이면 예외가 발생한다")
    void updateWithNullRating() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(null, "내용");

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("리뷰 수정 시 내용이 null이면 예외가 발생한다")
    void updateWithNullContent() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(5, null);

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void register_평점이_1미만이면_예외발생() {
        // given
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(0, "평점이 잘못된 리뷰");

        // when & then
        assertThatThrownBy(() -> Review.register(request, prompt, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다");
    }

    @Test
    void register_평점이_5초과이면_예외발생() {
        // given
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(6, "평점이 잘못된 리뷰");

        // when & then
        assertThatThrownBy(() -> Review.register(request, prompt, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다");
    }

    @Test
    void update_평점이_1미만이면_예외발생() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(0, "수정된 내용");

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다");
    }

    @Test
    void update_평점이_5초과이면_예외발생() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(6, "수정된 내용");

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 범위여야 합니다");
    }
}