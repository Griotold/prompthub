package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.review.Review;
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

    @Embedded
    private Rating rating = Rating.empty();

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
        prompt.rating = Rating.empty();
        prompt.isPublic = true;

        return prompt;
    }

    public void makePrivate() {
        this.isPublic = false;
    }

    public void makePublic() {
        this.isPublic = true;
    }

    public void update(PromptUpdateRequest request) {
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

    public void increaseLikeCount() {
        this.likesCount++;
    }

    public void decreaseLikeCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }

    // Rating 관련 메서드들
    public void addRating(Review review) {
        this.rating = this.rating.add(review);
    }

    public void removeRating(Review review) {
        this.rating = this.rating.remove(review);
    }

    public Double getAverageRating() {
        return rating.getAverage();
    }

    public boolean hasReviews() {
        return rating.hasReviews();
    }

    public Integer getReviewsCount() {
        return rating.reviewsCount();
    }
}
