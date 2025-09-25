package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.tag.Tag;

import java.util.List;

/**
 * 프롬프트-태그 연결 조회를 담당하는 인터페이스
 */
public interface PromptTagFinder {

    /**
     * 프롬프트에 연결된 태그들을 조회
     * @param prompt 프롬프트 엔티티
     * @return 연결된 태그 목록
     */
    List<Tag> findTagsByPrompt(Prompt prompt);
}
