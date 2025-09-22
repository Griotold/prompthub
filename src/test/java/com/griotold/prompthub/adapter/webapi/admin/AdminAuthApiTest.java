package com.griotold.prompthub.adapter.webapi.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.support.annotation.IntegrationTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@IntegrationTest
@RequiredArgsConstructor
class AdminAuthApiTest {

    final MockMvcTester mvcTester;
    final ObjectMapper objectMapper;
    final MemberRepository memberRepository;
    final PasswordEncoder passwordEncoder;

    Member admin;
    Member user;
    Member deactivatedAdmin;

    @BeforeEach
    void setUp() {
        // 활성 관리자
        admin = memberRepository.save(
                Member.createAdmin("admin@prompthub.app", "admin123", "관리자", passwordEncoder)
        );

        // 일반 사용자
        user = memberRepository.save(
                MemberFixture.createGoogleMember("user@prompthub.app", "일반사용자")
        );

        // 비활성화된 관리자
        deactivatedAdmin = Member.createAdmin("deactivated@prompthub.app", "admin123", "비활성관리자", passwordEncoder);
        deactivatedAdmin.deactivate();
        memberRepository.save(deactivatedAdmin);
    }

    @Test
    void login_성공() throws JsonProcessingException, UnsupportedEncodingException {
        // Given: 올바른 관리자 로그인 요청
        AdminLoginRequest request = new AdminLoginRequest("관리자", "admin123");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 관리자 로그인 API 호출
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 성공 응답과 JWT 토큰 반환
        assertThat(result)
                .hasStatusOk()
                .bodyJson()
                .hasPathSatisfying("$.success", success -> assertThat(success).isEqualTo(true))
                .hasPathSatisfying("$.data.accessToken", token -> assertThat(token).isNotNull())
                .hasPathSatisfying("$.data.refreshToken", token -> assertThat(token).isNotNull());

        // 응답 데이터 검증
        String responseBody = result.getResponse().getContentAsString();
        // BaseResponse 구조에 맞게 data 필드에서 LoginResponse 추출
        // 실제 BaseResponse 구조에 따라 JSON path 조정 필요
        assertThat(responseBody).contains("accessToken");
        assertThat(responseBody).contains("refreshToken");
    }

    @Test
    void login_존재하지_않는_관리자() throws JsonProcessingException {
        // Given: 존재하지 않는 닉네임
        AdminLoginRequest request = new AdminLoginRequest("존재하지않는관리자", "admin123");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request with Problem Details format
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo(400))
                .hasPathSatisfying("$.detail", detail ->
                        assertThat(detail).asString().contains("관리자를 찾을 수 없습니다"));
    }

    @Test
    void login_일반사용자_권한없음() throws JsonProcessingException {
        // Given: 일반 사용자 닉네임으로 로그인 시도
        AdminLoginRequest request = new AdminLoginRequest("일반사용자", "password");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request with Problem Details format
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo(400))
                .hasPathSatisfying("$.detail", detail ->
                        assertThat(detail).asString().contains("관리자 권한이 없습니다"));
    }

    @Test
    void login_비활성화된_계정() throws JsonProcessingException {
        // Given: 비활성화된 관리자 계정
        AdminLoginRequest request = new AdminLoginRequest("비활성관리자", "admin123");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request
        // Then: 400 Bad Request with Problem Details format
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo(400))
                .hasPathSatisfying("$.detail", detail ->
                        assertThat(detail).asString().contains("비활성화된 계정입니다"));
    }

    @Test
    void login_잘못된_비밀번호() throws JsonProcessingException {
        // Given: 올바른 닉네임, 잘못된 비밀번호
        AdminLoginRequest request = new AdminLoginRequest("관리자", "wrongpassword");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request with Problem Details format
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson()
                .hasPathSatisfying("$.status", status -> assertThat(status).isEqualTo(400))
                .hasPathSatisfying("$.detail", detail ->
                        assertThat(detail).asString().contains("비밀번호가 일치하지 않습니다"));
    }

    @Test
    void login_필수값_검증_닉네임_없음() throws JsonProcessingException {
        // Given: 닉네임이 없는 요청
        AdminLoginRequest request = new AdminLoginRequest("", "admin123");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_필수값_검증_비밀번호_없음() throws JsonProcessingException {
        // Given: 비밀번호가 없는 요청
        AdminLoginRequest request = new AdminLoginRequest("관리자", "");
        String requestJson = objectMapper.writeValueAsString(request);

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                .exchange();

        // Then: 400 Bad Request (Validation Error)
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_잘못된_JSON_형식() {
        // Given: 잘못된 JSON 형식
        String invalidJson = "{ invalid json }";

        // When: 로그인 시도
        MvcTestResult result = mvcTester.post()
                .uri("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .exchange();

        // Then: 400 Bad Request
        assertThat(result)
                .apply(print())
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}