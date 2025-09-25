package com.griotold.prompthub.application.prompt;

import com.griotold.prompthub.application.category.provided.CategoryFinder;
import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.prompt.provided.PromptTagFinder;
import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.prompt.response.PromptListResponse;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromptQueryService implements PromptFinder {

    private final PromptRepository promptRepository;
    private final PromptLikeRepository  promptLikeRepository;
    private final PromptTagFinder promptTagFinder;
    private final CategoryFinder categoryFinder;


    @Override
    public Prompt find(Long promptId) {
        return promptRepository.findByIdWithCategoryAndMember(promptId)
                .orElseThrow(() -> new IllegalArgumentException("프롬프트를 찾을 수 없습니다. id: " + promptId));
    }

    /**
     * 공개 프롬프트 조회 (메인 페이지)
     * */
    @Override
    public Page<Prompt> findAllPublic(Pageable pageable) {
        return promptRepository.findAllPublic(pageable);
    }

    /**
     * 카테고리별 조회
     * */
    @Override
    public Page<Prompt> findAllPublicByCategory(Category category, Pageable pageable) {
        return promptRepository.findAllPublicByCategory(category, pageable);
    }

    /**
     * 자신의 프롬프트 조회
     * */
    @Override
    public Page<Prompt> findAllByMember(Member member, Pageable pageable) {
        return promptRepository.findAllByMember(member, pageable);
    }

    /**
     * 검색 - 타이틀 키워드, 내용 키워드
     * */
    @Override
    public Page<Prompt> searchPublic(String keyword, Pageable pageable) {
        return promptRepository.searchPublic(keyword, pageable);
    }

    /**
     * 특정 프롬프트에 대한 사용자의 좋아요 여부 확인
     * 프론트에서 UI 표시를 위해
     * */
    @Override
    public boolean isLikedBy(Long promptId, Member member) {
        Prompt prompt = find(promptId);
        return promptLikeRepository.existsByPromptAndMember(prompt, member);
    }

    /**
     * 사용자가 좋아요한 프롬프트 목록
     * */
    @Override
    public Page<Prompt> findLikedByMember(Member member, Pageable pageable) {
        return promptRepository.findLikedByMember(member, pageable);
    }

    @Override
    public Page<PromptListResponse> findPublicPrompts(Long categoryId, String keyword, Pageable pageable) {
        Page<Prompt> prompts;

        // 기존 분기 로직을 Application 계층으로 이동
        if (categoryId != null) {
            Category category = categoryFinder.find(categoryId);
            prompts = promptRepository.findAllPublicByCategory(category, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            prompts = promptRepository.searchPublic(keyword, pageable);
        } else {
            prompts = promptRepository.findAllPublic(pageable);
        }

        // 🎉 태그 조회 없이 바로 DTO 변환 (N+1 문제 해결!)
        return prompts.map(PromptListResponse::of);
    }

    @Override
    public Page<PromptListResponse> findPopularPrompts(Pageable pageable) {
        Page<Prompt> prompts = promptRepository.findPopular(pageable);
        return prompts.map(PromptListResponse::of);
    }
}
