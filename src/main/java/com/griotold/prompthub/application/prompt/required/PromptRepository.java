package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    // 공개 프롬프트 조회 (메인 페이지)
    Page<Prompt> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    // 카테고리별 조회
    Page<Prompt> findByCategoryAndIsPublicTrueOrderByCreatedAtDesc(Category category, Pageable pageable);

    // 사용자별 프롬프트 (마이페이지)
    Page<Prompt> findByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    // 검색 (제목, 내용)
    Page<Prompt> findByTitleContainingOrContentContainingAndIsPublicTrueOrderByCreatedAtDesc(
            String titleKeyword, String contentKeyword, Pageable pageable);

    // 인기 프롬프트 (좋아요순)
    Page<Prompt> findByIsPublicTrueOrderByLikesCountDescCreatedAtDesc(Pageable pageable);

    // 특정 프롬프트의 좋아요 여부 확인
    boolean existsByIdAndPromptLikes_Member(Long promptId, Member member);
}
