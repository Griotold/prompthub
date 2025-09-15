package com.griotold.prompthub.application.tag;

import com.griotold.prompthub.application.tag.provided.TagFinder;
import com.griotold.prompthub.application.tag.provided.TagRegister;
import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagNames;
import com.griotold.prompthub.domain.tag.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class TagModifyService implements TagRegister {

    private final TagFinder tagFinder;
    private final TagRepository tagRepository;

    @Override
    public Tag register(Tag tag) {
        return  tagRepository.save(tag);
    }

    @Override
    public List<Tag> ensureTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return List.of();
        }

        // 0. 입력 태그명 중복 제거
        TagNames distinctTagNames = TagNames.ofDistinct(tagNames);

        // 1. 기존에 있는 태그들 조회
        Tags existingTags = Tags.of(tagFinder.findByNames(distinctTagNames.toList()));

        // 2. 없는 태그명들 추출
        TagNames newTagNames = distinctTagNames.filterExisting(existingTags.extractNames());

        // 3. 새 태그들 생성 및 저장
        Tags savedNewTags = Tags.of(tagRepository.saveAll(newTagNames.createTags()));

        // 4. 기존 + 새로 생성된 태그들 모두 반환
        return existingTags.combine(savedNewTags).toList();
    }
}
