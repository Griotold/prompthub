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
    Prompt updateInfo(Long promptId, @Valid PromptUpdateRequest request, Member currentMember);

    // 공개/비공개 설정
    Prompt makePublic(Long promptId, Member currentMember);
    Prompt makePrivate(Long promptId, Member currentMember);

    // 카테고리 변경
    Prompt changeCategory(Long promptId, Category category, Member currentMember);

    // 조회수 증가
    Prompt increaseViewCount(Long promptId);

    // 좋아요 관리
    void addLike(Long promptId, Member member);
    void removeLike(Long promptId, Member member);
}
