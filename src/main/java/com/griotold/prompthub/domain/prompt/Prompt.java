package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
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
@Table(name = "p_prompt")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Prompt extends AbstractEntity {

    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Integer viewsCount = 0;

    @Column(nullable = false)
    private Integer likesCount = 0;

    @Column(precision = 3, scale = 2, columnDefinition = "DECIMAL(3,2) DEFAULT 0.0")
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Integer reviewsCount = 0;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Prompt register(PromptRegisterRequest request, Member member, Category category) {
        Prompt prompt = new Prompt();

        prompt.title = requireNonNull(request.title());
        prompt.content = requireNonNull(request.content());
        prompt.description = request.description();
        prompt.member = requireNonNull(member);
        prompt.category = requireNonNull(category);
        prompt.viewsCount = 0;
        prompt.likesCount = 0;
        prompt.averageRating = 0.0;
        prompt.reviewsCount = 0;
        prompt.isPublic = true;

        return prompt;
    }

    public void makePrivate() {
        this.isPublic = false;
    }

    public void makePublic() {
        this.isPublic = true;
    }

    public void updateInfo(PromptUpdateRequest request) {
        this.title = requireNonNull(request.title());
        this.content = requireNonNull(request.content());
        this.description = request.description();
    }

    public void increaseViewCount() {
        this.viewsCount++;
    }

    public boolean isOwnedBy(Member member) {
        return this.member.equals(member);
    }

    public void changeCategory(Category category) {
        this.category = requireNonNull(category);
    }

    public void increaseLikeCount() {
        this.likesCount++;
    }

    public void decreaseLikeCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    public void updateRatingInfo(Double newAverageRating, Integer newReviewsCount) {
        this.averageRating = newAverageRating != null ? newAverageRating : 0.0;
        this.reviewsCount = newReviewsCount != null ? newReviewsCount : 0;
    }

    public void increaseReviewCount() {
        this.reviewsCount++;
    }

    public void decreaseReviewCount() {
        if (this.reviewsCount > 0) {
            this.reviewsCount--;
        }
    }
}
