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
import com.griotold.prompthub.domain.review.ReviewRegisterRequest;
import com.griotold.prompthub.domain.review.ReviewUpdateRequest;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ApplicationTest
@RequiredArgsConstructor
class ReviewRegisterTest {
    final ReviewRegister reviewRegister;
    final ReviewFinder reviewFinder;
    final ReviewRepository reviewRepository;
    final PromptRepository promptRepository;
    final MemberRepository memberRepository;
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
    void register() {
        // Given: 리뷰 등록 요청
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, "훌륭한 프롬프트입니다");

        // 프롬프트 초기 상태 확인
        assertThat(prompt1.getReviewsCount()).isEqualTo(0);
        assertThat(prompt1.getAverageRating()).isEqualTo(0.0);
        assertThat(prompt1.hasReviews()).isFalse();

        // When: 리뷰 등록
        Review savedReview = reviewRegister.register(request, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: 리뷰가 저장되고 프롬프트 평점이 업데이트됨
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getRating()).isEqualTo(5);
        assertThat(savedReview.getContent()).isEqualTo("훌륭한 프롬프트입니다");
        assertThat(savedReview.getMember()).isEqualTo(reviewer1);
        assertThat(savedReview.getPrompt().getId()).isEqualTo(prompt1.getId());

        // 프롬프트 평점 업데이트 확인
        Prompt afterReview = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterReview.getReviewsCount()).isEqualTo(1);
        assertThat(afterReview.getAverageRating()).isEqualTo(5.0);
        assertThat(afterReview.hasReviews()).isTrue();
    }

    @Test
    void register_중복_리뷰_예외() {
        // Given: 이미 리뷰를 작성한 상태
        ReviewRegisterRequest firstRequest = ReviewFixture.createReviewRegisterRequest(4, "첫 번째 리뷰");
        reviewRegister.register(firstRequest, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // When & Then: 같은 사용자가 같은 프롬프트에 다시 리뷰 작성 시도
        ReviewRegisterRequest secondRequest = ReviewFixture.createReviewRegisterRequest(5, "두 번째 리뷰");
        Prompt updatedPrompt = promptRepository.findById(prompt1.getId()).get();

        assertThatThrownBy(() -> reviewRegister.register(secondRequest, updatedPrompt, reviewer1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 프롬프트에 대한 리뷰를 작성했습니다");
    }

    @Test
    void register_여러_사용자_리뷰() {
        // Given: 두 명의 사용자가 같은 프롬프트에 리뷰 작성
        ReviewRegisterRequest request1 = ReviewFixture.createReviewRegisterRequest(5, "훌륭합니다");
        ReviewRegisterRequest request2 = ReviewFixture.createReviewRegisterRequest(3, "괜찮습니다");

        Prompt prompt = promptRepository.findById(prompt1.getId()).get();

        // When: 첫 번째 리뷰 등록
        reviewRegister.register(request1, prompt, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // 중간 상태 확인
        Prompt afterFirstReview = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterFirstReview.getReviewsCount()).isEqualTo(1);
        assertThat(afterFirstReview.getAverageRating()).isEqualTo(5.0);

        // 두 번째 리뷰 등록
        reviewRegister.register(request2, afterFirstReview, reviewer2);
        entityManager.flush();
        entityManager.clear();

        // Then: 두 리뷰 모두 반영된 평점
        Prompt afterSecondReview = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterSecondReview.getReviewsCount()).isEqualTo(2);
        assertThat(afterSecondReview.getAverageRating()).isEqualTo(4.0); // (5+3)/2
    }

    @Test
    void register_다른_프롬프트에는_영향없음() {
        // Given: prompt1에 리뷰 등록
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(4, "좋은 프롬프트");
        Prompt prompt1Updated = promptRepository.findById(prompt1.getId()).get();

        // prompt2 초기 상태 확인
        Prompt prompt2Before = promptRepository.findById(prompt2.getId()).get();
        assertThat(prompt2Before.getReviewsCount()).isEqualTo(0);

        // When: prompt1에만 리뷰 등록
        reviewRegister.register(request, prompt1Updated, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: prompt1만 변경되고 prompt2는 영향 없음
        Prompt prompt1After = promptRepository.findById(prompt1.getId()).get();
        Prompt prompt2After = promptRepository.findById(prompt2.getId()).get();

        assertThat(prompt1After.getReviewsCount()).isEqualTo(1);
        assertThat(prompt1After.getAverageRating()).isEqualTo(4.0);

        assertThat(prompt2After.getReviewsCount()).isEqualTo(0);
        assertThat(prompt2After.getAverageRating()).isEqualTo(0.0);
    }

    /**
     * update
     * */
    @Test
    void update() {
        // Given: 기존 리뷰 등록
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(3, "보통입니다");
        Review existingReview = reviewRegister.register(registerRequest, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인
        Prompt beforeUpdate = promptRepository.findById(prompt1.getId()).get();
        assertThat(beforeUpdate.getReviewsCount()).isEqualTo(1);
        assertThat(beforeUpdate.getAverageRating()).isEqualTo(3.0);

        // When: 리뷰 수정
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(5, "수정: 훌륭합니다");
        Review updatedReview = reviewRegister.update(existingReview.getId(), updateRequest, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: 리뷰 내용 변경되고 프롬프트 평점 업데이트
        assertThat(updatedReview.getId()).isEqualTo(existingReview.getId());
        assertThat(updatedReview.getRating()).isEqualTo(5);
        assertThat(updatedReview.getContent()).isEqualTo("수정: 훌륭합니다");

        // 프롬프트 평점 업데이트 확인
        Prompt afterUpdate = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterUpdate.getReviewsCount()).isEqualTo(1); // 개수는 동일
        assertThat(afterUpdate.getAverageRating()).isEqualTo(5.0); // 평점만 변경
    }

    @Test
    void update_권한_없음_예외() {
        // Given: reviewer1이 작성한 리뷰
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "좋습니다");
        Review existingReview = reviewRegister.register(registerRequest, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // When & Then: reviewer2가 수정 시도
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(5, "수정 시도");

        assertThatThrownBy(() -> reviewRegister.update(existingReview.getId(), updateRequest, reviewer2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 리뷰만 수정할 수 있습니다");
    }

    @Test
    void update_존재하지_않는_리뷰() {
        // Given: 존재하지 않는 리뷰 ID
        Long nonExistentReviewId = 999L;
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(5, "수정 시도");

        // When & Then: 존재하지 않는 리뷰 수정 시도
        assertThatThrownBy(() -> reviewRegister.update(nonExistentReviewId, updateRequest, reviewer1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다");
    }

    @Test
    void update_여러_리뷰_중_하나만_변경() {
        // Given: 두 개의 리뷰 등록
        ReviewRegisterRequest request1 = ReviewFixture.createReviewRegisterRequest(3, "보통");
        ReviewRegisterRequest request2 = ReviewFixture.createReviewRegisterRequest(5, "훌륭함");

        Review review1 = reviewRegister.register(request1, prompt1, reviewer1);
        Review review2 = reviewRegister.register(request2, prompt1, reviewer2);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인 (3+5)/2 = 4.0
        Prompt beforeUpdate = promptRepository.findById(prompt1.getId()).get();
        assertThat(beforeUpdate.getReviewsCount()).isEqualTo(2);
        assertThat(beforeUpdate.getAverageRating()).isEqualTo(4.0);

        // When: review1만 수정 (3 -> 1)
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(1, "수정: 별로");
        reviewRegister.update(review1.getId(), updateRequest, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: 평점 다시 계산 (1+5)/2 = 3.0
        Prompt afterUpdate = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterUpdate.getReviewsCount()).isEqualTo(2); // 개수는 동일
        assertThat(afterUpdate.getAverageRating()).isEqualTo(3.0); // 평점 변경
    }

    @Test
    void update_다른_프롬프트에는_영향없음() {
        // Given: prompt1, prompt2에 각각 리뷰 등록
        ReviewRegisterRequest request1 = ReviewFixture.createReviewRegisterRequest(4, "prompt1 리뷰");
        ReviewRegisterRequest request2 = ReviewFixture.createReviewRegisterRequest(3, "prompt2 리뷰");

        Review review1 = reviewRegister.register(request1, prompt1, reviewer1);
        Review review2 = reviewRegister.register(request2, prompt2, reviewer2);
        entityManager.flush();
        entityManager.clear();

        // When: prompt1의 리뷰만 수정
        ReviewUpdateRequest updateRequest = ReviewFixture.createReviewUpdateRequest(5, "수정된 리뷰");
        reviewRegister.update(review1.getId(), updateRequest, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: prompt1만 변경되고 prompt2는 영향 없음
        Prompt prompt1After = promptRepository.findById(prompt1.getId()).get();
        Prompt prompt2After = promptRepository.findById(prompt2.getId()).get();

        assertThat(prompt1After.getAverageRating()).isEqualTo(5.0); // 변경됨
        assertThat(prompt2After.getAverageRating()).isEqualTo(3.0); // 기존 상태 유지
    }

    /**
     * delete
     * */
    @Test
    void delete() {
        // Given: 기존 리뷰 등록
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "좋은 프롬프트");
        Review existingReview = reviewRegister.register(registerRequest, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인
        Prompt beforeDelete = promptRepository.findById(prompt1.getId()).get();
        assertThat(beforeDelete.getReviewsCount()).isEqualTo(1);
        assertThat(beforeDelete.getAverageRating()).isEqualTo(4.0);
        assertThat(beforeDelete.hasReviews()).isTrue();

        // When: 리뷰 삭제
        reviewRegister.delete(existingReview.getId(), reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: 리뷰 삭제되고 프롬프트 평점 초기화
        assertThatThrownBy(() -> reviewFinder.find(existingReview.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다");

        // 프롬프트 평점 초기화 확인
        Prompt afterDelete = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterDelete.getReviewsCount()).isEqualTo(0);
        assertThat(afterDelete.getAverageRating()).isEqualTo(0.0);
        assertThat(afterDelete.hasReviews()).isFalse();
    }

    @Test
    void delete_권한_없음_예외() {
        // Given: reviewer1이 작성한 리뷰
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(3, "보통입니다");
        Review existingReview = reviewRegister.register(registerRequest, prompt1, reviewer1);
        entityManager.flush();
        entityManager.clear();

        // When & Then: reviewer2가 삭제 시도
        assertThatThrownBy(() -> reviewRegister.delete(existingReview.getId(), reviewer2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("본인이 작성한 리뷰만 삭제할 수 있습니다");
    }

    @Test
    void delete_존재하지_않는_리뷰() {
        // Given: 존재하지 않는 리뷰 ID
        Long nonExistentReviewId = 999L;

        // When & Then: 존재하지 않는 리뷰 삭제 시도
        assertThatThrownBy(() -> reviewRegister.delete(nonExistentReviewId, reviewer1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("리뷰를 찾을 수 없습니다");
    }

    @Test
    void delete_여러_리뷰_중_하나만_삭제() {
        // Given: 두 개의 리뷰 등록
        ReviewRegisterRequest request1 = ReviewFixture.createReviewRegisterRequest(5, "훌륭함");
        ReviewRegisterRequest request2 = ReviewFixture.createReviewRegisterRequest(3, "보통");

        Review review1 = reviewRegister.register(request1, prompt1, reviewer1);
        Review review2 = reviewRegister.register(request2, prompt1, reviewer2);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인 (5+3)/2 = 4.0
        Prompt beforeDelete = promptRepository.findById(prompt1.getId()).get();
        assertThat(beforeDelete.getReviewsCount()).isEqualTo(2);
        assertThat(beforeDelete.getAverageRating()).isEqualTo(4.0);

        // When: review1만 삭제
        reviewRegister.delete(review1.getId(), reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: review2만 남음
        Prompt afterDelete = promptRepository.findById(prompt1.getId()).get();
        assertThat(afterDelete.getReviewsCount()).isEqualTo(1);
        assertThat(afterDelete.getAverageRating()).isEqualTo(3.0); // review2만 남음

        // review1은 삭제됨
        assertThatThrownBy(() -> reviewFinder.find(review1.getId()))
                .isInstanceOf(IllegalArgumentException.class);

        // review2는 여전히 존재
        Review remainingReview = reviewFinder.find(review2.getId());
        assertThat(remainingReview.getRating()).isEqualTo(3);
    }

    @Test
    void delete_다른_프롬프트에는_영향없음() {
        // Given: prompt1, prompt2에 각각 리뷰 등록
        ReviewRegisterRequest request1 = ReviewFixture.createReviewRegisterRequest(4, "prompt1 리뷰");
        ReviewRegisterRequest request2 = ReviewFixture.createReviewRegisterRequest(5, "prompt2 리뷰");

        Review review1 = reviewRegister.register(request1, prompt1, reviewer1);
        Review review2 = reviewRegister.register(request2, prompt2, reviewer2);
        entityManager.flush();
        entityManager.clear();

        // When: prompt1의 리뷰만 삭제
        reviewRegister.delete(review1.getId(), reviewer1);
        entityManager.flush();
        entityManager.clear();

        // Then: prompt1만 영향받고 prompt2는 그대로
        Prompt prompt1After = promptRepository.findById(prompt1.getId()).get();
        Prompt prompt2After = promptRepository.findById(prompt2.getId()).get();

        assertThat(prompt1After.getReviewsCount()).isEqualTo(0);
        assertThat(prompt1After.getAverageRating()).isEqualTo(0.0);

        assertThat(prompt2After.getReviewsCount()).isEqualTo(1);
        assertThat(prompt2After.getAverageRating()).isEqualTo(5.0); // 기존 상태 유지
    }
}