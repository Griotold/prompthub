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

        assertThatThrownBy(() -> member.reactivate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 활성화된 계정입니다");
    }
}