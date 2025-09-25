package com.griotold.prompthub.application.prompt;

import com.griotold.prompthub.application.prompt.provided.PromptTagFinder;
import com.griotold.prompthub.application.prompt.required.PromptTagRepository;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptTag;
import com.griotold.prompthub.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PromptTagQueryService implements PromptTagFinder {

    private final PromptTagRepository promptTagRepository;

    @Override
    public List<Tag> findTagsByPrompt(Prompt prompt) {
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt);
        return promptTags.stream()
                .map(PromptTag::getTag)
                .toList();
    }
}
