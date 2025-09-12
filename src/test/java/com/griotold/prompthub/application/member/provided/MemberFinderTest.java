package com.griotold.prompthub.application.member.provided;

import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.Provider;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
record MemberFinderTest(MemberFinder memberFinder,
                        MemberRepository memberRepository,
                        EntityManager entityManager) {

    @Test
    void find() {
        // given
        Member member = createAndSaveEmailMember("test@test.com", "testnick");

        // when
        Member found = memberFinder.find(member.getId());

        // then
        assertThat(found.getEmail().address()).isEqualTo("test@test.com");
        assertThat(found.getNickname()).isEqualTo("testnick");
        assertThat(found.isActive()).isTrue();
    }

    @Test
    void find_존재하지_않는_회원() {
        // when & then
        assertThatThrownBy(() -> memberFinder.find(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원을 찾을 수 없습니다. id: 999");
    }

    @Test
    void findBySocial_기존_구글_계정_존재() {
        // given
        Member googleMember = createAndSaveGoogleMember("google@test.com", "구글사용자");

        // when
        Optional<Member> found = memberFinder.findBySocial(Provider.GOOGLE, googleMember.getProviderId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("구글사용자");
        assertThat(found.get().getProvider()).isEqualTo(Provider.GOOGLE);
    }

    @Test
    void findBySocial_존재하지_않는_소셜_계정() {
        // when
        Optional<Member> found = memberFinder.findBySocial(Provider.GOOGLE, "notfound123");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void findBySocial_같은_providerId_다른_provider() {
        // given
        Member googleMember = createAndSaveGoogleMember("test@test.com", "구글사용자");

        // when
        Optional<Member> googleFound = memberFinder.findBySocial(Provider.GOOGLE, googleMember.getProviderId());
        Optional<Member> naverFound = memberFinder.findBySocial(Provider.NAVER, googleMember.getProviderId());

        // then
        assertThat(googleFound).isPresent();
        assertThat(naverFound).isEmpty();
    }

    private Member createAndSaveEmailMember(String email, String nickname) {
        Member member = Member.register(
                MemberFixture.createMemberRegisterRequest(email, "password123", "password123", nickname),
                MemberFixture.createPasswordEncoder()
        );
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private Member createAndSaveGoogleMember(String email, String nickname) {
        Member member = MemberFixture.createGoogleMember(email, nickname);
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();
        return member;
    }
}