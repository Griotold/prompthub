package com.griotold.prompthub.application.member.provided;

import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberStatus;
import com.griotold.prompthub.domain.member.Provider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
record MemberRegisterTest(MemberRegister memberRegister,
                          MemberRepository memberRepository,
                          EntityManager entityManager) {

    @Test
    void registerWithSocial_구글() {
        // given
        var socialRequest = MemberFixture.createGoogleSocialRequest("google@test.com", "구글사용자");

        // when
        Member registered = memberRegister.registerWithSocial(socialRequest);

        // then
        assertThat(registered.getId()).isNotNull();
        assertThat(registered.getEmail().address()).isEqualTo("google@test.com");
        assertThat(registered.getNickname()).isEqualTo("구글사용자");
        assertThat(registered.getProvider()).isEqualTo(Provider.GOOGLE);
        assertThat(registered.getProviderId()).contains("google_");
        assertThat(registered.getPasswordHash()).isEqualTo("SOCIAL_LOGIN");
        assertThat(registered.getEmailVerified()).isTrue();
        assertThat(registered.isActive()).isTrue();
    }

    @Test
    void reactivate() {
        // given
        Member member = createAndSaveGoogleMember("test@test.com", "테스트사용자");
        member.deactivate();
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        // when
        Member reactivated = memberRegister.reactivate(member.getId());

        // then
        assertThat(reactivated.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(reactivated.getDeactivatedAt()).isNull();
    }

    @Test
    void reactivate_이미_활성화된_계정() {
        // given
        Member activeMember = createAndSaveGoogleMember("test@test.com", "테스트사용자");

        // when & then
        assertThatThrownBy(() -> memberRegister.reactivate(activeMember.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 활성화된 계정입니다");
    }

    private Member createAndSaveGoogleMember(String email, String nickname) {
        Member member = MemberFixture.createGoogleMember(email, nickname);
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();
        return member;
    }
}