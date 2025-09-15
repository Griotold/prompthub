package com.griotold.prompthub.application.tag;

import com.griotold.prompthub.application.tag.provided.TagFinder;
import com.griotold.prompthub.application.tag.provided.TagRegister;
import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
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
        List<String> distinctTagNames = getDistinctTagNames(tagNames);

        // 1. 기존에 있는 태그들 조회
        List<Tag> existingTags = tagFinder.findByNames(distinctTagNames);
        List<String> existingTagNames = extractTagNames(existingTags);

        // 2. 없는 태그명들 추출
        List<String> newTagNames = findNewTagNames(distinctTagNames, existingTagNames);

        // 3. 새 태그들 생성 및 저장
        List<Tag> savedNewTags = createAndSaveNewTags(newTagNames);

        // 4. 기존 + 새로 생성된 태그들 모두 반환
        return combineExistingAndNewTags(existingTags, savedNewTags);
    }

    private List<String> getDistinctTagNames(List<String> tagNames) {
        return tagNames.stream()
                .distinct().toList();
    }

    private List<String> extractTagNames(List<Tag> tags) {
        return tags.stream()
                .map(Tag::getName)
                .toList();
    }

    private List<String> findNewTagNames(List<String> allTagNames, List<String> existingTagNames) {
        return allTagNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .toList();
    }

    private List<Tag> createAndSaveNewTags(List<String> newTagNames) {
        List<Tag> newTags = newTagNames.stream()
                .map(Tag::create)
                .toList();
        return tagRepository.saveAll(newTags);
    }

    private List<Tag> combineExistingAndNewTags(List<Tag> existingTags, List<Tag> newTags) {
        List<Tag> allTags = new ArrayList<>(existingTags);
        allTags.addAll(newTags);
        return allTags;
    }

}
