package com.griotold.prompthub.application.tag.provided;

import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagNames;
import com.griotold.prompthub.domain.tag.Tags;


public interface TagRegister {
    /**
     * 태그 저장 (개별)
     */
    Tag register(Tag tag);

    /**
     * 태그명들을 받아서 존재하지 않는 것들만 생성 후 모든 태그 반환
     * 핵심 메서드: 프롬프트 등록 시 사용
     */
    Tags ensureTags(TagNames tagNames);
}
