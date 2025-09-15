package com.griotold.prompthub.application.tag.provided;

import com.griotold.prompthub.domain.tag.Tag;

import java.util.List;

public interface TagFinder {
    /**
     * ID로 태그 조회
     */
    Tag find(Long tagId);

    /**
     * 태그명으로 단일 태그 조회
     */
    Tag findByName(String name);

    /**
     * 여러 태그명으로 한번에 조회
     * 프롬프트 등록 시 기존 태그들 찾기용
     */
    List<Tag> findByNames(List<String> names);


}
