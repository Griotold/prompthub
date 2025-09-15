package com.griotold.prompthub.application.review.provided;

import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.review.required.ReviewRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewFixture;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
@RequiredArgsConstructor
class ReviewFinderTest {

    final ReviewFinder reviewFinder;
    final ReviewRepository reviewRepository;
    final MemberRepository memberRepository;
    final PromptRepository promptRepository;
    final CategoryRepository categoryRepository;
    final EntityManager entityManager;

    Category category;
    Member author;
    Member reviewer1;
    Member reviewer2;
    Prompt prompt1;
    Prompt prompt2;

    @BeforeEach
    void setUp() {
        // 조연들 미리 준비
        category = categoryRepository.save(CategoryFixture.createCategory("AI", "AI 카테고리"));
        author = memberRepository.save(MemberFixture.createGoogleMember("author@prompthub.app", "작성자"));
        reviewer1 = memberRepository.save(MemberFixture.createNaverMember("reviewer1@prompthub.app", "리뷰어1"));
        reviewer2 = memberRepository.save(MemberFixture.createKakaoMember("reviewer2@prompthub.app", "리뷰어2"));
        prompt1 = promptRepository.save(PromptFixture.createPrompt(author, category));
        prompt2 = promptRepository.save(PromptFixture.createAnotherPrompt(author, category));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void find() {
        // Given: 리뷰 생성
        Review review = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // When
        Review found = reviewFinder.find(review.getId());

        // Then
        assertThat(found.getId()).isEqualTo(review.getId());
        assertThat(found.getRating()).isEqualTo(review.getRating());
        assertThat(found.getContent()).isEqualTo(review.getContent());
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void find_없는_id일때() {
        assertThatThrownBy(() -> reviewFinder.find(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다. id: 999");
    }

    @Test
    void findByPrompt() {
        // Given: 같은 프롬프트에 대한 여러 리뷰들
        Review review1 = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer1));
        Review review2 = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer2));

        // 비교군: prompt2에 대한 리뷰들 (조회되면 안 됨)
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer1));
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer2));

        entityManager.flush();
        entityManager.clear();

        // When
        List<Review> found = reviewFinder.findByPrompt(prompt1);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Review::getId)
                .containsExactlyInAnyOrder(review1.getId(), review2.getId());
    }

    @Test
    void findByMember() {
        // Given: 같은 사용자가 작성한 여러 리뷰들
        Review review1 = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer1));
        Review review2 = reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer1));

        // 비교군: reviewer2가 작성한 리뷰들 (조회되면 안 됨)
        reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer2));
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer2));

        entityManager.flush();
        entityManager.clear();

        // When
        List<Review> found = reviewFinder.findByMember(reviewer1);

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).extracting(Review::getId)
                .containsExactlyInAnyOrder(review1.getId(), review2.getId());
    }

    @Test
    void findByPromptAndMember() {
        // Given: 특정 프롬프트-사용자 조합의 리뷰
        Review review = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer1));

        // 비교군: 다른 조합들 (조회되면 안 됨)
        reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer2)); // 같은 프롬프트, 다른 사용자
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer1)); // 다른 프롬프트, 같은 사용자
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer2)); // 다른 프롬프트, 다른 사용자

        entityManager.flush();
        entityManager.clear();

        // When
        Review found = reviewFinder.findByPromptAndMember(prompt1, reviewer1);

        // Then
        assertThat(found.getId()).isEqualTo(review.getId());
        assertThat(found.getRating()).isEqualTo(review.getRating());
        assertThat(found.getContent()).isEqualTo(review.getContent());
    }

    @Test
    void findByPromptAndMember_없을때() {
        // Given: 리뷰가 없는 상태

        // When & Then
        assertThatThrownBy(() -> reviewFinder.findByPromptAndMember(prompt1, reviewer1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 프롬프트에 대한 리뷰를 찾을 수 없습니다");
    }

    @Test
    void findByPromptExcludingMember() {
        // Given: author의 리뷰(제외 대상)와 다른 사용자들의 리뷰들
        reviewRepository.save(ReviewFixture.createReview(prompt1, author)); // 제외될 리뷰
        Review review1 = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer1));
        Review review2 = reviewRepository.save(ReviewFixture.createReview(prompt1, reviewer2));

        // 비교군: prompt2에 대한 리뷰들 (조회되면 안 됨)
        reviewRepository.save(ReviewFixture.createReview(prompt2, author));
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer1));
        reviewRepository.save(ReviewFixture.createReview(prompt2, reviewer2));

        entityManager.flush();
        entityManager.clear();

        // When: author를 제외한 리뷰 조회
        Slice<Review> found = reviewFinder.findByPromptExcludingMember(
                prompt1, author, PageRequest.of(0, 10));

        // Then: author의 리뷰는 제외되고 다른 사용자들의 리뷰만 조회
        assertThat(found.getContent()).hasSize(2);
        assertThat(found.getContent()).extracting(Review::getId)
                .containsExactlyInAnyOrder(review1.getId(), review2.getId());
    }
}