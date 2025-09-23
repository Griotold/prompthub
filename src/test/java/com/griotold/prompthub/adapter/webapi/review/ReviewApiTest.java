package com.griotold.prompthub.adapter.webapi.review;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.review.provided.ReviewRegister;
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
import com.griotold.prompthub.support.annotation.IntegrationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    final ReviewRegister reviewRegister;

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

    /**
     * getPromptReviews
     * */
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

    /**
     * register
     * */
    @Test
    void register_성공() throws Exception {
        // Given: 리뷰 등록 요청
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, "정말 유용한 프롬프트입니다!");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 리뷰 등록 API 호출
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 성공 응답
        assertThat(result).hasStatusOk();

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"success\":true");
        assertThat(responseBody).contains("\"rating\":5");
        assertThat(responseBody).contains("\"content\":\"정말 유용한 프롬프트입니다!\"");
        assertThat(responseBody).contains("\"authorNickname\":\"리뷰어1\"");
    }

    @Test
    void register_중복_리뷰_등록_실패() throws Exception {
        // Given: 이미 리뷰를 등록한 상태
        reviewRepository.save(ReviewFixture.createExcellentReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 새로운 리뷰 등록 시도
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(4, "또 다른 리뷰");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 중복 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("이미 해당 프롬프트에 대한 리뷰를 작성했습니다");
    }

    @Test
    void register_존재하지_않는_프롬프트() throws Exception {
        // Given: 존재하지 않는 프롬프트 ID
        Long nonExistentPromptId = 999L;
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, "좋은 프롬프트");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 존재하지 않는 프롬프트에 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", nonExistentPromptId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("프롬프트를 찾을 수 없습니다");
    }

    @Test
    void register_인증_토큰_없음() throws Exception {
        // Given: 유효한 리뷰 등록 요청, 하지만 인증 토큰 없음
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, "좋은 프롬프트");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 인증 없이 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 401 Unauthorized
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void register_유효성_검증_실패_평점_범위초과() throws Exception {
        // Given: 잘못된 평점 (6점)
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(6, "좋은 프롬프트");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 잘못된 평점으로 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_유효성_검증_실패_평점_0점() throws Exception {
        // Given: 잘못된 평점 (0점)
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(0, "좋은 프롬프트");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 잘못된 평점으로 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_유효성_검증_실패_내용_빈값() throws Exception {
        // Given: 빈 내용
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, "");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 빈 내용으로 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_유효성_검증_실패_내용_길이초과() throws Exception {
        // Given: 500자 초과 내용
        String longContent = "a".repeat(501);
        ReviewRegisterRequest request = ReviewFixture.createReviewRegisterRequest(5, longContent);
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 긴 내용으로 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void register_잘못된_JSON_형식() throws Exception {
        // Given: 잘못된 JSON 형식
        String invalidJson = "{ invalid json }";

        // When: 잘못된 JSON으로 리뷰 등록 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/prompts/{promptId}/reviews", prompt.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    /**
     * update
     * */
    @Test
    void update_성공() throws Exception {
        // Given: 실제 API 플로우와 동일하게 리뷰 등록
        // ReviewFixture 대신 실제 서비스를 통해 리뷰 등록
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "기존 리뷰");
        Review existingReview = reviewRegister.register(registerRequest, prompt, reviewer1);

        entityManager.flush();

        // 수정 요청
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, "수정된 리뷰 내용입니다. 더욱 좋아졌네요!");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 리뷰 수정 API 호출
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 성공 응답
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data.rating", rating -> assertThat(rating).isEqualTo(5))
                .hasPathSatisfying("$.data.content", content ->
                        assertThat(content).isEqualTo("수정된 리뷰 내용입니다. 더욱 좋아졌네요!"))
                .hasPathSatisfying("$.data.authorNickname", nickname ->
                        assertThat(nickname).isEqualTo("리뷰어1"));
    }

    @Test
    void update_존재하지_않는_리뷰() throws Exception {
        // Given: 존재하지 않는 리뷰 ID
        Long nonExistentReviewId = 999L;
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, "수정된 내용");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 존재하지 않는 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", nonExistentReviewId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("리뷰를 찾을 수 없습니다");
    }

    @Test
    void update_권한_없음_다른_사용자_리뷰() throws Exception {
        // Given: reviewer2가 작성한 리뷰
        Review otherUserReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer2));
        entityManager.flush();
        entityManager.clear();

        // reviewer1이 reviewer2의 리뷰를 수정하려고 시도
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, "남의 리뷰 수정 시도");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 다른 사용자의 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", otherUserReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("본인이 작성한 리뷰만 수정할 수 있습니다");
    }

    @Test
    void update_인증_토큰_없음() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, "수정된 내용");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 인증 없이 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 401 Unauthorized
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void update_유효성_검증_실패_평점_범위초과() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 잘못된 평점 (6점)
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(6, "수정된 내용");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 잘못된 평점으로 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_유효성_검증_실패_평점_0점() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 잘못된 평점 (0점)
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(0, "수정된 내용");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 잘못된 평점으로 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_유효성_검증_실패_내용_빈값() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 빈 내용
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, "");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 빈 내용으로 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_유효성_검증_실패_내용_길이초과() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 500자 초과 내용
        String longContent = "a".repeat(501);
        ReviewUpdateRequest request = ReviewFixture.createReviewUpdateRequest(5, longContent);
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 긴 내용으로 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_잘못된_JSON_형식() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        Review existingReview = reviewRepository.save(ReviewFixture.createGoodReview(prompt, reviewer1));
        entityManager.flush();
        entityManager.clear();

        // 잘못된 JSON 형식
        String invalidJson = "{ invalid json }";

        // When: 잘못된 JSON으로 리뷰 수정 시도
        MvcTestResult result = mvcTester.put()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    /**
     * delete
     * */
    @Test
    void delete_성공() throws Exception {
        // Given: 실제 API 플로우와 동일하게 리뷰 등록
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "삭제될 리뷰");
        Review existingReview = reviewRegister.register(registerRequest, prompt, reviewer1);

        entityManager.flush();

        // When: 리뷰 삭제 API 호출
        MvcTestResult result = mvcTester.delete()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 성공 응답
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data", data -> assertThat(data).isNull());
    }

    @Test
    void delete_존재하지_않는_리뷰() throws Exception {
        // Given: 존재하지 않는 리뷰 ID
        Long nonExistentReviewId = 999L;

        // When: 존재하지 않는 리뷰 삭제 시도
        MvcTestResult result = mvcTester.delete()
                .uri("/api/v1/reviews/{reviewId}", nonExistentReviewId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("리뷰를 찾을 수 없습니다");
    }

    @Test
    void delete_권한_없음_다른_사용자_리뷰() throws Exception {
        // Given: reviewer2가 작성한 리뷰
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "다른 사용자의 리뷰");
        Review otherUserReview = reviewRegister.register(registerRequest, prompt, reviewer2);

        entityManager.flush();

        // reviewer1이 reviewer2의 리뷰를 삭제하려고 시도
        // When: 다른 사용자의 리뷰 삭제 시도
        MvcTestResult result = mvcTester.delete()
                .uri("/api/v1/reviews/{reviewId}", otherUserReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reviewer1Token)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("본인이 작성한 리뷰만 삭제할 수 있습니다");
    }

    @Test
    void delete_인증_토큰_없음() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "삭제될 리뷰");
        Review existingReview = reviewRegister.register(registerRequest, prompt, reviewer1);

        entityManager.flush();

        // When: 인증 없이 리뷰 삭제 시도
        MvcTestResult result = mvcTester.delete()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .exchange();

        // Then: 401 Unauthorized
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void delete_잘못된_토큰() throws Exception {
        // Given: 기존 리뷰가 있는 상태
        ReviewRegisterRequest registerRequest = ReviewFixture.createReviewRegisterRequest(4, "삭제될 리뷰");
        Review existingReview = reviewRegister.register(registerRequest, prompt, reviewer1);

        entityManager.flush();

        // 잘못된 JWT 토큰
        String invalidToken = "invalid.jwt.token";

        // When: 잘못된 토큰으로 리뷰 삭제 시도
        MvcTestResult result = mvcTester.delete()
                .uri("/api/v1/reviews/{reviewId}", existingReview.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .exchange();

        // Then: 401 Unauthorized
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }
}