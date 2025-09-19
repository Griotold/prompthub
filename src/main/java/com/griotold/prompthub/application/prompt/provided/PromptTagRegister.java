package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.domain.prompt.Prompt;

import java.util.List;

/**
 * 프롬프트-태그 연결 관리를 담당하는 인터페이스
 */
public interface PromptTagRegister {

    /**
     * 프롬프트에 태그 이름들을 연결 (태그가 없으면 자동 생성)
     * @param prompt 프롬프트 엔티티
     * @param tagNameList 연결할 태그 이름들
     */
    void linkTagsByNames(Prompt prompt, List<String> tagNameList);

    /**
     * 프롬프트의 태그들을 업데이트 (기존 태그 모두 삭제 후 새로 연결)
     * @param prompt 프롬프트 엔티티
     * @param tagNameList 새로운 태그 이름들
     */
    void updateTagsByNames(Prompt prompt, List<String> tagNameList);

    /**
     * 프롬프트의 모든 태그 연결 해제
     * @param prompt 프롬프트 엔티티
     */
    void unlinkAllTags(Prompt prompt);
}