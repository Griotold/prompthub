package com.griotold.prompthub.application.tag;

import com.griotold.prompthub.application.tag.provided.TagFinder;
import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class TagQueryService implements TagFinder {

    private final TagRepository tagRepository;

    @Override
    public Tag find(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다. id: " + tagId));
    }

    @Override
    public Tag findByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("태그를 찾을 수 없습니다. name: " + name));
    }

    @Override
    public List<Tag> findByNames(List<String> names) {
        return tagRepository.findByNameIn(names);
    }

}
