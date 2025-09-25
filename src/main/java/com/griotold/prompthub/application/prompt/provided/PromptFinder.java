package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.application.prompt.response.PromptDetailResponse;
import com.griotold.prompthub.application.prompt.response.PromptListResponse;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromptFinder {
    Prompt find(Long promptId);

    Page<Prompt> findAllPublic(Pageable pageable);

    Page<Prompt> findAllPublicByCategory(Category category, Pageable pageable);

    Page<Prompt> searchPublic(String keyword, Pageable pageable);

    boolean isLikedBy(Long promptId, Member member);

    Page<Prompt> findLikedByMember(Member member, Pageable pageable);

    // 🆕 새로운 통합 메서드 추가 (기존 메서드는 그대로 유지)
    /**
     * 공개 프롬프트 목록 조회 (통합) - 태그 정보 포함
     * @param categoryId 카테고리 ID (optional)
     * @param keyword 검색 키워드 (optional)
     * @param pageable 페이징 정보
     * @return 프롬프트 목록 응답 (태그 포함)
     */
    Page<PromptListResponse> findPublicPrompts(Long categoryId, String keyword, Pageable pageable);
    Page<PromptListResponse> findPopularPrompts(Pageable pageable);
    PromptDetailResponse getPromptDetail(Long promptId, Member member);
    Page<PromptListResponse> findAllByMember(Member member, Pageable pageable);

}
