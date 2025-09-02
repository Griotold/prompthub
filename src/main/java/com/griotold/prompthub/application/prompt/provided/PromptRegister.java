package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptRegisterRequest;
import com.griotold.prompthub.domain.prompt.PromptUpdateRequest;
import jakarta.validation.Valid;

public interface PromptRegister {
    // 기본 CRUD
    Prompt register(@Valid PromptRegisterRequest registerRequest, Member member, Category category);
    Prompt updateInfo(Long promptId, @Valid PromptUpdateRequest request);

    // 공개/비공개 설정
    Prompt makePublic(Long promptId);
    Prompt makePrivate(Long promptId);

    // 카테고리 변경
    Prompt changeCategory(Long promptId, Category category);

    // 조회수 증가
    Prompt increaseViewCount(Long promptId);

    // 좋아요 관리
    void addLike(Long promptId, Member member);
    void removeLike(Long promptId, Member member);
}
