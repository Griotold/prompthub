package com.griotold.prompthub.application.prompt;

import com.griotold.prompthub.application.prompt.provided.PromptTagRegister;
import com.griotold.prompthub.application.prompt.required.PromptTagRepository;
import com.griotold.prompthub.application.tag.provided.TagRegister;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptTag;
import com.griotold.prompthub.domain.tag.TagNames;
import com.griotold.prompthub.domain.tag.Tags;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class PromptTagModifyService implements PromptTagRegister {

    private final PromptTagRepository promptTagRepository;
    private final TagRegister tagRegister;

    @Override
    public void linkTagsByNames(Prompt prompt, List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            return;
        }

        // List<String> -> TagNames 변환 (내부에서 처리)
        TagNames tagNames = TagNames.of(tagNameList);

        // 태그들 존재 확인 및 자동 생성
         Tags tags = tagRegister.ensureTags(tagNames);

        // 프롬프트-태그 연결 생성 및 저장
        List<PromptTag> promptTags = tags.createLinksTo(prompt);
        List<PromptTag> newPromptTags = filterNewLinks(promptTags, prompt);

        promptTagRepository.saveAll(newPromptTags);
    }

    @Override
    public void updateTagsByNames(Prompt prompt, List<String> tagNames) {

    }

    @Override
    public void unlinkAllTags(Prompt prompt) {

    }

    /**
     * 이미 연결된 태그는 제외하고 새로 연결할 PromptTag만 반환
     */
    private List<PromptTag> filterNewLinks(List<PromptTag> promptTags, Prompt prompt) {
        return promptTags.stream()
                .filter(pt -> !promptTagRepository.existsByPromptAndTag(prompt, pt.getTag()))
                .toList();
    }

}
