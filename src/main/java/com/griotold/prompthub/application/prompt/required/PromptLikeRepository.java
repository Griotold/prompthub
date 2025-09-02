package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptLikeRepository extends JpaRepository<PromptLike, Long> {

    boolean existsByPromptAndMember(Prompt prompt, Member member);
    void deleteByPromptAndMember(Prompt prompt, Member member);

    // 추가로 유용할 것 같은 메서드들
    //long countByPrompt(Prompt prompt);  // 특정 프롬프트의 좋아요 수
    //List<PromptLike> findByMember(Member member);  // 특정 회원이 좋아요한 목록
}
