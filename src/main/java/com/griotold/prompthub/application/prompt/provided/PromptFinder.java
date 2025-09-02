package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromptFinder {
    /** ID로 단건 조회(없으면 예외) */
    Prompt find(Long promptId);

    Page<Prompt> findAllPublic(Pageable pageable);

    Page<Prompt> findAllPublicByCategory(Category category, Pageable pageable);

    Page<Prompt> findAllByMember(Member member, Pageable pageable);

    Page<Prompt> searchPublic(String keyword, Pageable pageable);

    Page<Prompt> findPopular(Pageable pageable);

    boolean isLikedBy(Long promptId, Member member);
}
