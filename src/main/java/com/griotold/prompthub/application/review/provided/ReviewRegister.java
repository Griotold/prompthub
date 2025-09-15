package com.griotold.prompthub.application.review.provided;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewRegisterRequest;
import com.griotold.prompthub.domain.review.ReviewUpdateRequest;
import jakarta.validation.Valid;

public interface ReviewRegister {

    /**
     * 리뷰 등록
     * 중복 등록 방지: 동일한 프롬프트에 대해 한 사용자는 하나의 리뷰만 작성 가능
     */
    Review register(@Valid ReviewRegisterRequest request, Prompt prompt, Member member);

    /**
     * 리뷰 수정
     * 본인이 작성한 리뷰만 수정 가능
     */
    Review update(Long reviewId, @Valid ReviewUpdateRequest request, Member member);

    /**
     * 리뷰 삭제
     * 본인이 작성한 리뷰만 삭제 가능
     */
    void delete(Long reviewId, Member member);
}