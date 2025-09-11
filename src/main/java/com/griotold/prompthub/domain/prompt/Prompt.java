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

    @Column(nullable = false)                 // 5.0 -> 50, 3.8 -> 38
    private Integer averageRatingValue = 0; // 10배 저장

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
        prompt.averageRatingValue = 0;
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

    /**
     * 새로운 리뷰 평점이 추가될 때 호출
     * @param rating 새로 추가된 평점 (1~5 범위 가정)
     */
    public void addRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 범위여야 합니다.");
        }

        int totalScore = this.averageRatingValue * this.reviewsCount; // 10배 저장되어 있으므로 그대로 사용 가능
        int newTotalScore = totalScore + (rating * 10); // 새 평점도 10배 변환
        int newReviewsCount = this.reviewsCount + 1;

        this.averageRatingValue = newTotalScore / newReviewsCount;
        this.reviewsCount = newReviewsCount;
    }

    /**
     * 리뷰가 삭제될 때 호출
     * @param rating 삭제된 리뷰의 평점 (1~5 범위 가정)
     */
    public void removeRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 범위여야 합니다.");
        }
        if (this.reviewsCount == 0) {
            throw new IllegalStateException("삭제할 리뷰가 없습니다.");
        }

        int totalScore = this.averageRatingValue * this.reviewsCount; // 현재 총합
        int newTotalScore = totalScore - (rating * 10);
        int newReviewsCount = this.reviewsCount - 1;

        if (newReviewsCount == 0) {
            this.averageRatingValue = 0;
            this.reviewsCount = 0;
            return;
        }

        this.averageRatingValue = newTotalScore / newReviewsCount;
        this.reviewsCount = newReviewsCount;
    }


    public Double getAverageRating() {
        return averageRatingValue / 10.0;
    }
}
