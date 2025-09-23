package com.griotold.prompthub.adapter.webapi.review;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
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
import com.griotold.prompthub.support.annotation.IntegrationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@IntegrationTest
@RequiredArgsConstructor
class ReviewApiTest {

    final MockMvcTester mvcTester;
    final ObjectMapper objectMapper;
    final MemberRepository memberRepository;
    final CategoryRepository categoryRepository;
    final PromptRepository promptRepository;
    final ReviewRepository reviewRepository;
    final JwtTokenProvider jwtTokenProvider;
    final EntityManager entityManager;

    Category category;
    Member author;
    Member reviewer1;
    Member reviewer2;
    Member reviewer3;
    Prompt prompt;
    String reviewer1Token;
    String reviewer2Token;

    @BeforeEach
    void setUp() {
        // 카테고리 및 사용자들 준비
        category = categoryRepository.save(CategoryFixture.createCategory("AI", "AI 카테고리"));
        author = memberRepository.save(MemberFixture.createGoogleMember("author@prompthub.app", "작성자"));
        reviewer1 = memberRepository.save(MemberFixture.createNaverMember("reviewer1@prompthub.app", "리뷰어1"));
        reviewer2 = memberRepository.save(MemberFixture.createKakaoMember("reviewer2@prompthub.app", "리뷰어2"));
        reviewer3 = memberRepository.save(MemberFixture.createGoogleMember("reviewer3@prompthub.app", "리뷰어3"));

        // 프롬프트 생성
        prompt = promptRepository.save(PromptFixture.createPrompt(author, category));

        // JWT 토큰 생성
        reviewer1Token = jwtTokenProvider.createAccessToken(reviewer1);
        reviewer2Token = jwtTokenProvider.createAccessToken(reviewer2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void getPromptReviews_성공_내_리뷰가_첫번째() throws JsonProcessingException, UnsupportedEncodingException {
        // Given: reviewer1의 리뷰와 다른 사용자들의 리뷰들
        Review myReview = reviewRepository.save(ReviewFixture.createExcellentReview(prompt, reviewer1));
        Review otherReview1 = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer2));
        Review otherReview2 = reviewRepository.save(ReviewFixture.createAverageReview(prompt, reviewer3));

        entityManager.flush();
        entityManager.clear();

        // When: reviewer1이 프롬프트 리뷰 목록 조회
        MvcTestResult result = mvcTester.get()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 성공 응답과 내 리뷰가 첫 번째에 위치
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data.reviews", reviews -> assertThat(reviews).isNotNull())
                .hasPathSatisfying("$.data.hasNext", hasNext -> assertThat(hasNext).isEqualTo(false));
    }

    @Test
    void getPromptReviews_성공_내_리뷰가_없는_경우() throws JsonProcessingException {
        // Given: reviewer1은 리뷰를 작성하지 않고, 다른 사용자들만 리뷰 작성
        Review otherReview1 = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer2));
        Review otherReview2 = reviewRepository.save(ReviewFixture.createAverageReview(prompt, reviewer3));

        entityManager.flush();
        entityManager.clear();

        // When: reviewer1이 프롬프트 리뷰 목록 조회
        MvcTestResult result = mvcTester.get()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 성공 응답과 다른 사용자들의 리뷰만 조회
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data.reviews", reviews -> assertThat(reviews).asList().hasSize(2))
                .hasPathSatisfying("$.data.hasNext", hasNext -> assertThat(hasNext).isEqualTo(false));
    }

    @Test
    void getPromptReviews_페이징_동작_확인() throws JsonProcessingException {
        // Given: reviewer1의 리뷰와 충분한 다른 사용자들의 리뷰들 (페이징 테스트용)
        reviewRepository.save(ReviewFixture.createExcellentReview(prompt, reviewer1));
        reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer2));
        reviewRepository.save(ReviewFixture.createAverageReview(prompt, reviewer3));

        // 추가 사용자들과 리뷰들
        Member reviewer4 = memberRepository.save(MemberFixture.createNaverMember("reviewer4@prompthub.app", "리뷰어4"));
        Member reviewer5 = memberRepository.save(MemberFixture.createKakaoMember("reviewer5@prompthub.app", "리뷰어5"));
        reviewRepository.save(ReviewFixture.createPoorReview(prompt, reviewer4));
        reviewRepository.save(ReviewFixture.createBadReview(prompt, reviewer5));

        entityManager.flush();
        entityManager.clear();

        // When: size=3으로 첫 페이지 조회 (내 리뷰 + 다른 리뷰 2개)
        MvcTestResult result = mvcTester.get()
                .uri("/api/v1/prompts/{promptId}/reviews?size=3", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 3개 조회되고 hasNext=true
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.data.reviews", reviews -> assertThat(reviews).asList().hasSize(3))
                .hasPathSatisfying("$.data.hasNext", hasNext -> assertThat(hasNext).isEqualTo(true));

        // 첫 번째가 내 리뷰인지 확인
        assertThat(result)
                .bodyJson()
                .hasPathSatisfying("$.data.reviews[0].authorNickname", nickname ->
                        assertThat(nickname).isEqualTo("리뷰어1"));
    }

    @Test
    void getPromptReviews_존재하지_않는_프롬프트() {
        // Given: 존재하지 않는 프롬프트 ID
        Long nonExistentPromptId = 999L;

        // When: 존재하지 않는 프롬프트의 리뷰 조회
        MvcTestResult result = mvcTester.get()
                .uri("/api/v1/prompts/{promptId}/reviews", nonExistentPromptId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 400 Bad Request with Problem Details format
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo(400))
                .hasPathSatisfying("$.detail", detail ->
                        assertThat(detail).asString().contains("프롬프트를 찾을 수 없습니다"));
    }

    @Test
    void getPromptReviews_빈_리뷰_목록() {
        // Given: 리뷰가 없는 프롬프트

        // When: 리뷰가 없는 프롬프트의 리뷰 목록 조회
        MvcTestResult result = mvcTester.get()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 빈 리스트 반환
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data.reviews", reviews -> assertThat(reviews).asList().hasSize(0))
                .hasPathSatisfying("$.data.hasNext", hasNext -> assertThat(hasNext).isEqualTo(false));
    }
}