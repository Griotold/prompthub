package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptTag;
import com.griotold.prompthub.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 태그 기반 추천, 태그 클라우드, 인기 태그 등의 기능을 고려하면 별도 리포지토리가 유리
 * */
public interface PromptTagRepository extends JpaRepository<PromptTag, Long> {
    // 프롬프트의 모든 태그 조회(페치 조인)
    @Query("SELECT pt FROM PromptTag pt JOIN FETCH pt.tag WHERE pt.prompt = :prompt")
    List<PromptTag> findByPromptWithTag(@Param("prompt") Prompt prompt);

    // 특정 태그를 가진 프롬프트들 조회(페치 조인)
    @Query("SELECT pt FROM PromptTag pt JOIN FETCH pt.prompt WHERE pt.tag = :tag")
    List<PromptTag> findByTagWithPrompt(@Param("tag") Tag tag);

    // 프롬프트-태그 조합 존재 여부 확인
    boolean existsByPromptAndTag(Prompt prompt, Tag tag);

    // 프롬프트의 기존 태그들 삭제 (태그 업데이트 시)
    void deleteByPrompt(Prompt prompt);
}
