package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptRegisterRequest;
import com.griotold.prompthub.domain.prompt.PromptUpdateRequest;
import com.griotold.prompthub.domain.review.Review;
import jakarta.validation.Valid;

public interface PromptRegister {
    // 기본 CRUD
    Prompt register(@Valid PromptRegisterRequest registerRequest, Member member, Category category);
    Prompt updateInfo(Long promptId, @Valid PromptUpdateRequest request, Member currentMember);

    // 공개/비공개 설정
    Prompt makePublic(Long promptId, Member currentMember);
    Prompt makePrivate(Long promptId, Member currentMember);

    // 조회수 증가
    Prompt increaseViewCount(Long promptId);

    // 좋아요 관리
    void addLike(Long promptId, Member member);
    void removeLike(Long promptId, Member member);

    /**
     * 리뷰 등록 시 프롬프트 평점 추가
     * TODO: 이벤트 발행으로 개선 예정 (ReviewRegisteredEvent)
     */
    void addReview(Prompt prompt, Review review);

    /**
     * 리뷰 수정 시 프롬프트 평점 업데이트
     * TODO: 이벤트 발행으로 개선 예정 (ReviewUpdatedEvent)
     */
    void updateReview(Prompt prompt, Integer oldRating, Integer newRating);

    /**
     * 리뷰 삭제 시 프롬프트 평점 제거
     * TODO: 이벤트 발행으로 개선 예정 (ReviewDeletedEvent)
     */
    void removeReview(Prompt prompt, Review review);
}
