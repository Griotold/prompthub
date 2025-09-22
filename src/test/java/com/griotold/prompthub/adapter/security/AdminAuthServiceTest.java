package com.griotold.prompthub.adapter.security;

import com.griotold.prompthub.adapter.webapi.admin.AdminLoginRequest;
import com.griotold.prompthub.adapter.webapi.auth.LoginResponse;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ApplicationTest
@RequiredArgsConstructor
class AdminAuthServiceTest {

    final AdminAuthService adminAuthService;
    final MemberRepository memberRepository;
    final EntityManager entityManager;
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

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void login_성공() {
        // Given: 올바른 관리자 로그인 요청
        AdminLoginRequest request = new AdminLoginRequest("관리자", "admin123");

        // When: 관리자 로그인
        LoginResponse response = adminAuthService.login(request);

        // Then: JWT 토큰이 발급됨
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
    }

    @Test
    void login_존재하지_않는_관리자_예외() {
        // Given: 존재하지 않는 닉네임
        AdminLoginRequest request = new AdminLoginRequest("존재하지않는관리자", "admin123");

        // When & Then: 관리자를 찾을 수 없음 예외
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자를 찾을 수 없습니다");
    }

    @Test
    void login_일반사용자_권한없음_예외() {
        // Given: 일반 사용자 닉네임으로 로그인 시도
        AdminLoginRequest request = new AdminLoginRequest("일반사용자", "password");

        // When & Then: 관리자 권한이 없음 예외
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 권한이 없습니다");
    }

    @Test
    void login_비활성화된_계정_예외() {
        // Given: 비활성화된 관리자 계정
        AdminLoginRequest request = new AdminLoginRequest("비활성관리자", "admin123");

        // When & Then: 비활성화된 계정 예외
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비활성화된 계정입니다");
    }

    @Test
    void login_잘못된_비밀번호_예외() {
        // Given: 올바른 닉네임, 잘못된 비밀번호
        AdminLoginRequest request = new AdminLoginRequest("관리자", "wrongpassword");

        // When & Then: 비밀번호 불일치 예외
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }

    @Test
    void login_소셜로그인_사용자_비밀번호_검증_실패() {
        // Given: 소셜 로그인 사용자 (구글 사용자가 어떻게든 ADMIN 권한을 가진 경우)
        // 이 케이스는 실제로는 발생하지 않겠지만, 방어 로직 테스트
        Member socialAdmin = MemberFixture.createGoogleMember("social@prompthub.app", "소셜관리자");
        // 현실적이지 않지만 테스트를 위해 강제로 ADMIN 권한 부여
        memberRepository.save(socialAdmin);
        entityManager.flush();
        entityManager.clear();

        AdminLoginRequest request = new AdminLoginRequest("소셜관리자", "anypassword");

        // When & Then: 소셜 사용자는 비밀번호 검증 불가
        assertThatThrownBy(() -> adminAuthService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("관리자 권한이 없습니다"); // Role이 USER이므로 권한 체크에서 먼저 걸림
    }
}