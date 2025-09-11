package com.griotold.prompthub.domain.review;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;

public class ReviewFixture {

    public static ReviewRegisterRequest createReviewRegisterRequest() {
        return new ReviewRegisterRequest(5, "정말 유용한 프롬프트입니다. 많은 도움이 되었어요!");
    }

    public static ReviewRegisterRequest createReviewRegisterRequest(Integer rating, String content) {
        return new ReviewRegisterRequest(rating, content);
    }

    public static ReviewUpdateRequest createReviewUpdateRequest() {
        return new ReviewUpdateRequest(4, "수정된 리뷰 내용입니다. 다시 사용해보니 좋네요.");
    }

    public static ReviewUpdateRequest createReviewUpdateRequest(Integer rating, String content) {
        return new ReviewUpdateRequest(rating, content);
    }

    public static Review createReview() {
        MemberRegisterRequest memberRegisterRequest = MemberFixture.createMemberRegisterRequest();
        PasswordEncoder passwordEncoder = MemberFixture.createPasswordEncoder();
        Member member = Member.register(memberRegisterRequest, passwordEncoder);
        Prompt prompt = PromptFixture.createPrompt();

        return Review.register(createReviewRegisterRequest(), prompt, member);
    }

    public static Review createReview(Integer rating, String content) {
        MemberRegisterRequest memberRegisterRequest = MemberFixture.createMemberRegisterRequest();
        PasswordEncoder passwordEncoder = MemberFixture.createPasswordEncoder();
        Member member = Member.register(memberRegisterRequest, passwordEncoder);
        Prompt prompt = PromptFixture.createPrompt();

        return Review.register(createReviewRegisterRequest(rating, content), prompt, member);
    }

    public static Review createReview(Prompt prompt, Member member) {
        return Review.register(createReviewRegisterRequest(), prompt, member);
    }

    public static Review createReview(ReviewRegisterRequest request, Prompt prompt, Member member) {
        return Review.register(request, prompt, member);
    }
}