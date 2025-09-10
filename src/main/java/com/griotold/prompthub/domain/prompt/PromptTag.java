package com.griotold.prompthub.domain.prompt;


import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@Table(name = "p_prompt_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_prompt_tag", columnNames = {"prompt_id", "tag_id"}))
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PromptTag extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreatedDate
    private LocalDateTime createdAt;

    public static PromptTag link(Prompt prompt, Tag tag) {
        PromptTag promptTag = new PromptTag();

        promptTag.prompt = requireNonNull(prompt);
        promptTag.tag = requireNonNull(tag);

        return promptTag;
    }
}
