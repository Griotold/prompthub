package com.griotold.prompthub.domain.review;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import org.junit.jupiter.api.BeforeEach;
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
    void update_리뷰를_수정하고_변경사항을_반환한다() {
        // 초기 상태 확인
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("정말 유용한 프롬프트입니다. 많은 도움이 되었어요!");

        // given
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(3, "수정된 리뷰 내용입니다");

        // when
        ReviewChange change = review.update(updateRequest);

        // then - 엔티티 상태 확인 (기존 테스트와 동일)
        assertThat(review.getRating()).isEqualTo(3);
        assertThat(review.getContent()).isEqualTo("수정된 리뷰 내용입니다");

        // then - ReviewChange 검증 (새로 추가)
        assertThat(change.oldRating()).isEqualTo(5);
        assertThat(change.newRating()).isEqualTo(3);
        assertThat(change.oldContent()).isEqualTo("정말 유용한 프롬프트입니다. 많은 도움이 되었어요!");
        assertThat(change.newContent()).isEqualTo("수정된 리뷰 내용입니다");
        assertThat(change.isRatingChanged()).isTrue();
        assertThat(change.isContentChanged()).isTrue();
        assertThat(change.hasAnyChange()).isTrue();
    }

    @Test
    void update_평점만_변경시_평점변경만_true() {
        // given
        String originalContent = review.getContent();
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(3, originalContent);

        // when
        ReviewChange change = review.update(updateRequest);

        // then
        assertThat(change.isRatingChanged()).isTrue();
        assertThat(change.isContentChanged()).isFalse();
        assertThat(change.hasAnyChange()).isTrue();
    }

    @Test
    void update_내용만_변경시_내용변경만_true() {
        // given
        Integer originalRating = review.getRating();
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(originalRating, "새로운 내용");

        // when
        ReviewChange change = review.update(updateRequest);

        // then
        assertThat(change.isRatingChanged()).isFalse();
        assertThat(change.isContentChanged()).isTrue();
        assertThat(change.hasAnyChange()).isTrue();
    }

    @Test
    void update_변경사항없음() {
        // given
        Integer originalRating = review.getRating();
        String originalContent = review.getContent();
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(originalRating, originalContent);

        // when
        ReviewChange change = review.update(updateRequest);

        // then
        assertThat(change.isRatingChanged()).isFalse();
        assertThat(change.isContentChanged()).isFalse();
        assertThat(change.hasAnyChange()).isFalse();
    }

    @Test
    void update_평점이_null이면_예외발생() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(null, "내용");

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void update_내용이_null이면_예외발생() {
        // given
        ReviewUpdateRequest updateRequest = new ReviewUpdateRequest(5, null);

        // when & then
        assertThatThrownBy(() -> review.update(updateRequest))
                .isInstanceOf(NullPointerException.class);
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

    @Test
    void isOwner_본인() {
        // given
        Member reviewer = review.getMember();

        // when
        boolean result = review.isOwner(reviewer);

        // then
        assertThat(result).isTrue();
    }

    @Test
    void isOwner_다른사람() {
        // given
        Member otherMember = Member.register(MemberFixture.createMemberRegisterRequest(), MemberFixture.createPasswordEncoder());

        // when
        boolean result = review.isOwner(otherMember);

        // then
        assertThat(result).isFalse();
    }

    @Test
    void isOwner_null() {
        // when
        boolean result = review.isOwner(null);

        // then
        assertThat(result).isFalse();
    }
}