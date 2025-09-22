package com.griotold.prompthub.domain.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.griotold.prompthub.domain.member.MemberFixture.createMemberRegisterRequest;
import static com.griotold.prompthub.domain.member.MemberFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class MemberTest {

    Member member;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        this.passwordEncoder = createPasswordEncoder();
        member = Member.register(createMemberRegisterRequest(), passwordEncoder);
    }

    @Test
    void registerMember() {
        assertThat(member.getRole()).isEqualTo(Role.USER);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getEmail().verified()).isFalse();
        assertThat(member.getRegisteredAt()).isNull();
        assertThat(member.getUpdatedAt()).isNull();
        assertThat(member.getDeactivatedAt()).isNull();
    }

    @Test
    void registerMember_비밀번호_비밀번호확인이_다를때() {
        assertThatThrownBy(() -> Member.register(createMemberRegisterRequest("password", "anotherPassoword"), passwordEncoder))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deactivate() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDeactivatedAt()).isNull();

        member.deactivate();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDeactivatedAt()).isNotNull();
    }

    @Test
    void deactive_이미_비활성화일때() {
        member.deactivate();
        assertThatThrownBy(() -> member.deactivate()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void verifyPassword() {
        assertThat(member.verifyPassword("secret3456", passwordEncoder)).isTrue();
        assertThat(member.verifyPassword("false", passwordEncoder)).isFalse();
    }

    @Test
    void invalidEmail() {
        assertThatThrownBy(() -> Member.register(createMemberRegisterRequest("invalid email"), passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);

        // 정상적인 이메일에는 예외가 터지지 않는다.
        Member.register(new MemberRegisterRequest("gotjd9773@naver.com", "secret123", "secret123", "nickname"),
                passwordEncoder);
    }

    @Test
    void verifyEmail() {
        assertThat(member.getEmail().verified()).isFalse();

        member.verifyEmail();

        assertThat(member.getEmail().verified()).isTrue();
    }

    @Test
    void verifyEmail_이미_인증된_상태에서_재인증() {
        member.verifyEmail();
        assertThat(member.getEmail().verified()).isTrue();

        // 다시 호출해도 문제없어야 함
        member.verifyEmail();
        assertThat(member.getEmail().verified()).isTrue();
    }

    @Test
    void isActive() {
        assertThat(member.isActive()).isTrue();

        member.deactivate();
        assertThat(member.isActive()).isFalse();
    }

    // 기존 테스트는 그대로 두고 아래 테스트들 추가

    @Test
    void registerWithSocial_구글() {
        SocialRegisterRequest request = MemberFixture.createGoogleSocialRequest("test@gmail.com", "구글사용자");
        Member socialMember = Member.registerWithSocial(request);

        assertThat(socialMember.getEmail().address()).isEqualTo("test@gmail.com");
        assertThat(socialMember.getNickname()).isEqualTo("구글사용자");
        assertThat(socialMember.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(socialMember.getProviderId()).isEqualTo("google_test@gmail.com");
        assertThat(socialMember.getPasswordHash()).isEqualTo("SOCIAL_LOGIN");
        assertThat(socialMember.getEmail().verified()).isTrue();
        assertThat(socialMember.getRole()).isEqualTo(Role.USER);
        assertThat(socialMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void isSocialUser() {
        Member emailMember = Member.register(createMemberRegisterRequest(), passwordEncoder);
        SocialRegisterRequest request = MemberFixture.createGoogleSocialRequest("test@gmail.com", "구글사용자");
        Member socialMember = Member.registerWithSocial(request);

        assertThat(emailMember.isSocialUser()).isFalse();
        assertThat(socialMember.isSocialUser()).isTrue();
    }

    @Test
    void verifyPassword_소셜사용자는_비밀번호_검증_불가() {
        SocialRegisterRequest request = MemberFixture.createGoogleSocialRequest("test@gmail.com", "구글사용자");
        Member socialMember = Member.registerWithSocial(request);

        assertThat(socialMember.verifyPassword("anypassword", passwordEncoder)).isFalse();
    }

    @Test
    void reactivate() {
        member.deactivate();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.DEACTIVATED);
        assertThat(member.getDeactivatedAt()).isNotNull();

        member.reactivate();

        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getDeactivatedAt()).isNull();
    }

    @Test
    void reactivate_이미_활성화된_계정() {
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);

        // 이미 활성화된 계정도 그냥 넘어간다
        member.reactivate();
    }

    @Test
    void createAdmin() {
        // given
        String email = "admin@prompthub.app";
        String password = "admin1234";
        String nickname = "Admin";

        // when
        Member admin = Member.createAdmin(email, password, nickname, passwordEncoder);

        // then
        assertThat(admin.getEmail().address()).isEqualTo(email);
        assertThat(admin.getNickname()).isEqualTo(nickname);
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN); // 핵심: ADMIN 역할
        assertThat(admin.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(admin.getEmail().verified()).isTrue(); // 관리자는 이메일 검증됨
        assertThat(admin.getProvider()).isNull(); // 소셜 로그인 아님
        assertThat(admin.getProviderId()).isNull();
        assertThat(admin.isSocialUser()).isFalse();

        // 비밀번호가 제대로 암호화되었는지 확인
        assertThat(admin.verifyPassword(password, passwordEncoder)).isTrue();
        assertThat(admin.verifyPassword("wrongpassword", passwordEncoder)).isFalse();
    }

    @Test
    void createAdmin_필수값_null체크() {
        String email = "admin@prompthub.app";
        String password = "admin1234";
        String nickname = "Admin";

        // nickname이 null일 때
        assertThatThrownBy(() -> Member.createAdmin(email, password, null, passwordEncoder))
                .isInstanceOf(NullPointerException.class);

        // password가 null일 때
        assertThatThrownBy(() -> Member.createAdmin(email, null, nickname, passwordEncoder))
                .isInstanceOf(NullPointerException.class);

        // passwordEncoder가 null일 때
        assertThatThrownBy(() -> Member.createAdmin(email, password, nickname, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void createAdmin_잘못된_이메일형식() {
        // given
        String invalidEmail = "invalid-email";
        String password = "admin1234";
        String nickname = "Admin";

        // when & then
        assertThatThrownBy(() -> Member.createAdmin(invalidEmail, password, nickname, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }
}