package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@Table(name = "p_prompt_like",
        uniqueConstraints = @UniqueConstraint (columnNames = {"member_id", "prompt_id"}))
@ToString(callSuper = true)
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PromptLike extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id")
    private Prompt prompt;

    @CreatedDate
    private LocalDateTime createdAt;

    public static PromptLike create(Member member, Prompt prompt) {
        PromptLike promptLike = new PromptLike();

        promptLike.member = requireNonNull(member);
        promptLike.prompt = requireNonNull(prompt);

        return promptLike;
    }
}
