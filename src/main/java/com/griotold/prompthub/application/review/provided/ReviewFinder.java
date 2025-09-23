package com.griotold.prompthub.application.review.provided;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface ReviewFinder {

    /**
     * ID로 리뷰 조회
     */
    Review find(Long reviewId);

    /**
     * ID로 리뷰 조회 + Member fetch join
     * */
    Review findWithMember(Long reviewId);

    /**
     * 프롬프트의 모든 리뷰 조회
     */
    List<Review> findByPrompt(Prompt prompt);

    /**
     * 사용자의 모든 리뷰 조회
     */
    List<Review> findByMember(Member member);

    /**
     * 특정 프롬프트에 특정 사용자가 작성한 리뷰 조회
     * (리뷰 중복 작성 방지 및 내 리뷰 조회용)
     */
    Review findByPromptAndMember(Prompt prompt, Member member);

    /**
     * 특정 사용자를 제외한 프롬프트의 리뷰들 조회 (페이징)
     *
     * 인프런 스타일 리뷰 시스템용:
     * - 내 리뷰는 최상단에 고정 표시
     * - 이 메서드는 다른 사람들의 리뷰만 페이징으로 조회
     * - 더보기 버튼 클릭 시마다 호출되어 추가 리뷰들을 로딩
     */
    Slice<Review> findByPromptExcludingMember(Prompt prompt, Member excludeMember, Pageable pageable);

    /**
     * 프롬프트의 리뷰들을 내 리뷰 우선으로 조회 (페이징)
     * - 첫 페이지: 내 리뷰 + 다른 사람들 리뷰 (size-1개)
     * - 이후 페이지: 다른 사람들 리뷰만
     */
    Slice<Review> findByPromptWithMyReviewFirst(Prompt prompt, Member member, Pageable pageable);
}