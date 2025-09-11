package com.griotold.prompthub.application.review.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewFixture;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DataJpaTest
@RequiredArgsConstructor
class ReviewRepositoryTest {

    final ReviewRepository reviewRepository;
    final EntityManager entityManager;

    Member member1, member2;
    Category category1, category2;
    Prompt prompt1, prompt2;
    Review review1, review2;

    @BeforeEach
    void setUp() {
        // 서로 다른 회원 생성
        member1 = MemberFixture.createGoogleMember("user1@test.com", "사용자1");
        member2 = MemberFixture.createGoogleMember("user2@test.com", "사용자2");

        // 서로 다른 카테고리 생성
        category1 = CategoryFixture.createCategory("카테고리1", "설명1");
        category2 = CategoryFixture.createCategory("카테고리2", "설명2");

        // 프롬프트들 생성
        prompt1 = PromptFixture.createPrompt("프롬프트1", "내용1", "설명1");
        prompt2 = PromptFixture.createPrompt("프롬프트2", "내용2", "설명2");

        // 기본 엔티티들 저장
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(category1);
        entityManager.persist(category2);

        // 프롬프트에 연관관계 설정
        ReflectionTestUtils.setField(prompt1, "member", member1);
        ReflectionTestUtils.setField(prompt1, "category", category1);
        ReflectionTestUtils.setField(prompt2, "member", member2);
        ReflectionTestUtils.setField(prompt2, "category", category2);

        entityManager.persist(prompt1);
        entityManager.persist(prompt2);

        entityManager.flush();
    }

    @Test
    void 동일한_멤버가_같은_프롬프트에_중복_리뷰시_예외발생() {
        // given
        review1 = ReviewFixture.createReview(prompt1, member1, 5);
        reviewRepository.save(review1);
        entityManager.flush();

        // when & then - 동일한 member, prompt로 두 번째 리뷰 저장 시도
        Review duplicateReview = ReviewFixture.createReview(prompt1, member1, 3);

        assertThatThrownBy(() -> {
            reviewRepository.save(duplicateReview);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByPrompt() {
        // 리뷰들 생성
        review1 = ReviewFixture.createReview(prompt1, member1, 5);
        review2 = ReviewFixture.createReview(prompt1, member2, 4);
        reviewRepository.saveAll(List.of(review1, review2));

        // prompt2에 추가 리뷰 생성 (노이즈 데이터)
        Review review3 = reviewRepository.save(ReviewFixture.createReview(prompt2, member1, 3));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findByPrompt(prompt1);

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(Review::getRating)
                .containsExactlyInAnyOrder(5, 4);
        assertThat(reviews)
                .extracting(r -> r.getPrompt().getTitle())
                .containsOnly("프롬프트1");
    }

    @Test
    void findByMember() {
        // given
        Review review1 = ReviewFixture.createReview(prompt1, member1, 5);
        Review review2 = ReviewFixture.createReview(prompt2, member1, 4);
        reviewRepository.saveAll(List.of(review1, review2));

        // member2의 리뷰 생성 (노이즈 데이터)
        Review review3 = ReviewFixture.createReview(prompt1, member2, 3);
        reviewRepository.save(review3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Review> reviews = reviewRepository.findByMember(member1);

        // then
        assertThat(reviews).hasSize(2);
        assertThat(reviews)
                .extracting(Review::getRating)
                .containsExactlyInAnyOrder(5, 4);
        assertThat(reviews)
                .extracting(r -> r.getMember().getNickname())
                .containsOnly("사용자1");
    }

    @Test
    void findByPromptAndMember() {
        // given
        Review review1 = ReviewFixture.createReview(prompt1, member1, 5);
        Review review2 = ReviewFixture.createReview(prompt1, member2, 4);
        Review review3 = ReviewFixture.createReview(prompt2, member1, 3);
        reviewRepository.saveAll(List.of(review1, review2, review3));

        entityManager.flush();
        entityManager.clear();

        // when & then
        // 존재하는 조합
        Optional<Review> found1 = reviewRepository.findByPromptAndMember(prompt1, member1);
        assertThat(found1).isPresent();
        assertThat(found1.get().getRating()).isEqualTo(5);

        // 존재하지 않는 조합
        Optional<Review> notFound = reviewRepository.findByPromptAndMember(prompt2, member2);
        assertThat(notFound).isEmpty();
    }

    @Test
    void findByPromptOrderByCreatedAtDesc() {
        // given
        Review review1 = ReviewFixture.createReview(prompt1, member1, 5);
        Review review2 = ReviewFixture.createReview(prompt1, member2, 4);
        reviewRepository.saveAll(List.of(review1, review2));

        // prompt2에 추가 리뷰 생성 (노이즈 데이터)
        Review review3 = ReviewFixture.createReview(prompt2, member1, 3);
        reviewRepository.save(review3);

        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Review> reviews = reviewRepository.findByPromptOrderByCreatedAtDesc(prompt1, pageable);

        // then
        assertThat(reviews.getContent()).hasSize(2);
        assertThat(reviews.hasNext()).isFalse();

        // prompt1의 리뷰들만 포함되는지 확인
        assertThat(reviews.getContent())
                .extracting(Review::getRating)
                .containsExactlyInAnyOrder(5, 4);
        assertThat(reviews.getContent())
                .extracting(r -> r.getPrompt().getTitle())
                .containsOnly("프롬프트1");

        // 최신순 정렬 확인 (createdAt이 나중인 것이 먼저)
        List<Review> reviewList = reviews.getContent();
        for (int i = 0; i < reviewList.size() - 1; i++) {
            assertThat(reviewList.get(i).getCreatedAt())
                    .isAfterOrEqualTo(reviewList.get(i + 1).getCreatedAt());
        }
    }

    @Test
    void findByPromptExcludingMember() {
        // given
        Review review1 = ReviewFixture.createReview(prompt1, member1, 5); // 제외될 리뷰
        Review review2 = ReviewFixture.createReview(prompt1, member2, 4); // 포함될 리뷰

        // 추가 멤버와 리뷰 생성
        Member member3 = MemberFixture.createGoogleMember("user3@test.com", "사용자3");
        entityManager.persist(member3);
        Review review3 = ReviewFixture.createReview(prompt1, member3, 3); // 포함될 리뷰

        // prompt2에 member1 리뷰 생성 (노이즈 데이터)
        Review review4 = ReviewFixture.createReview(prompt2, member1, 2);

        reviewRepository.saveAll(List.of(review1, review2, review3, review4));
        entityManager.flush();
        entityManager.clear();

        Pageable pageable = PageRequest.of(0, 10);

        // when - member1을 제외하고 prompt1의 리뷰 조회
        Slice<Review> reviews = reviewRepository.findByPromptExcludingMember(prompt1, member1, pageable);

        // then
        assertThat(reviews.getContent()).hasSize(2);
        assertThat(reviews.hasNext()).isFalse();

        // member1의 리뷰는 제외되고, member2, member3의 리뷰만 포함
        assertThat(reviews.getContent())
                .extracting(Review::getRating)
                .containsExactlyInAnyOrder(4, 3);
        assertThat(reviews.getContent())
                .extracting(r -> r.getMember().getNickname())
                .containsExactlyInAnyOrder("사용자2", "사용자3");

        // 모든 리뷰가 prompt1의 것인지 확인
        assertThat(reviews.getContent())
                .extracting(r -> r.getPrompt().getTitle())
                .containsOnly("프롬프트1");
    }
}