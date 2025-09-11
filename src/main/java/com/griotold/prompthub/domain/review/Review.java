package com.griotold.prompthub.domain.review;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@Table(name = "p_review",
        uniqueConstraints = @UniqueConstraint(name = "uk_member_prompt_review", columnNames = {"member_id", "prompt_id"}))
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private Integer rating; // 1-5 평점

    @Column(length = 500)
    private String content;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Review register(ReviewRegisterRequest registerRequest, Prompt prompt, Member member) {
        Review review = new Review();

        review.prompt = requireNonNull(prompt);
        review.member = requireNonNull(member);
        review.rating = requireNonNull(registerRequest.rating());
        review.content = requireNonNull(registerRequest.content());

        return review;
    }

    public void update(ReviewUpdateRequest updateRequest) {
        this.rating = requireNonNull(updateRequest.rating());
        this.content = requireNonNull(updateRequest.content());
    }
}