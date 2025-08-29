package com.griotold.prompthub.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.griotold.prompthub.domain.MemberFixture.createMemberRegisterRequest;
import static com.griotold.prompthub.domain.MemberFixture.createPasswordEncoder;
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
        assertThat(member.getEmailVerified()).isFalse();
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
        assertThat(member.getEmailVerified()).isFalse();

        member.verifyEmail();

        assertThat(member.getEmailVerified()).isTrue();
    }

    @Test
    void verifyEmail_이미_인증된_상태에서_재인증() {
        member.verifyEmail();
        assertThat(member.getEmailVerified()).isTrue();

        // 다시 호출해도 문제없어야 함
        member.verifyEmail();
        assertThat(member.getEmailVerified()).isTrue();
    }

    @Test
    void isActive() {
        assertThat(member.isActive()).isTrue();

        member.deactivate();
        assertThat(member.isActive()).isFalse();
    }
}