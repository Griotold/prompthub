package com.griotold.prompthub.application.member.required;

import com.griotold.prompthub.domain.member.*;
import com.griotold.prompthub.support.annotation.RepositoryTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@RepositoryTest
@RequiredArgsConstructor
class MemberRepositoryTest {

    final MemberRepository memberRepository;
    final EntityManager entityManager;

    Member member;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = MemberFixture.createPasswordEncoder();
        member = Member.register(
                MemberFixture.createMemberRegisterRequest("test@test.com", "password123", "password123", "testnick"),
                passwordEncoder
        );
        memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByEmail_Address() {
        Optional<Member> found = memberRepository.findByEmail_Address("test@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("testnick");
    }

    @Test
    void findByEmail_Address_존재하지_않는_이메일() {
        Optional<Member> found = memberRepository.findByEmail_Address("notfound@test.com");

        assertThat(found).isEmpty();
    }

    @Test
    void findByNickname() {
        Optional<Member> found = memberRepository.findByNickname("testnick");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail().address()).isEqualTo("test@test.com");
    }

    @Test
    void existsByEmail_Address() {
        boolean exists = memberRepository.existsByEmail_Address("test@test.com");
        boolean notExists = memberRepository.existsByEmail_Address("notfound@test.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void existsByNickname() {
        boolean exists = memberRepository.existsByNickname("testnick");
        boolean notExists = memberRepository.existsByNickname("notfound");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void findByStatus() {
        Member deactivatedMember = Member.register(
                MemberFixture.createMemberRegisterRequest("deact@test.com", "password123", "password123", "deactnick"),
                passwordEncoder
        );
        deactivatedMember.deactivate();
        memberRepository.save(deactivatedMember);

        List<Member> activeMembers = memberRepository.findByStatus(MemberStatus.ACTIVE);
        List<Member> deactivatedMembers = memberRepository.findByStatus(MemberStatus.DEACTIVATED);

        assertThat(activeMembers).hasSize(1);
        assertThat(activeMembers.getFirst().getNickname()).isEqualTo("testnick");
        assertThat(deactivatedMembers).hasSize(1);
        assertThat(deactivatedMembers.getFirst().getNickname()).isEqualTo("deactnick");
    }

    @Test
    void findByProviderAndProviderId_구글_소셜_로그인() {
        // given
        Member googleMember = MemberFixture.createGoogleMember("google@test.com", "구글사용자");
        memberRepository.save(googleMember);
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Member> found = memberRepository.findByProviderAndProviderId(Provider.GOOGLE, "google_google@test.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("구글사용자");
        assertThat(found.get().getProvider()).isEqualTo(Provider.GOOGLE);
    }

    @Test
    void findByProviderAndProviderId_존재하지_않는_소셜_계정() {
        // when
        Optional<Member> found = memberRepository.findByProviderAndProviderId(Provider.GOOGLE, "notfound123");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByRole_USER_역할() {
        // given - setUp()에서 이미 USER 역할 멤버 생성됨

        // when & then
        assertThat(memberRepository.existsByRole(Role.USER)).isTrue();
        assertThat(memberRepository.existsByRole(Role.ADMIN)).isFalse();
    }

    @Test
    void existsByRole_ADMIN_역할() {
        // given - 관리자 계정 생성
        Member admin = Member.createAdmin(
                "admin@prompthub.app",
                "admin1234",
                "Admin",
                passwordEncoder
        );
        memberRepository.save(admin);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThat(memberRepository.existsByRole(Role.ADMIN)).isTrue();
        assertThat(memberRepository.existsByRole(Role.USER)).isTrue(); // 기존 USER도 존재
    }

    @Test
    void existsByRole_역할별_여러_계정() {
        // given - 여러 역할의 계정들 생성
        Member admin1 = Member.createAdmin("admin1@prompthub.app", "admin1234", "Admin1", passwordEncoder);
        Member admin2 = Member.createAdmin("admin2@prompthub.app", "admin1234", "Admin2", passwordEncoder);
        Member user2 = Member.register(
                MemberFixture.createMemberRegisterRequest("user2@test.com", "password123", "password123", "user2"),
                passwordEncoder
        );

        memberRepository.saveAll(List.of(admin1, admin2, user2));
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThat(memberRepository.existsByRole(Role.USER)).isTrue(); // 2명 존재 (기존 + user2)
        assertThat(memberRepository.existsByRole(Role.ADMIN)).isTrue(); // 2명 존재 (admin1, admin2)
    }

    @Test
    void existsByRole_계정이_없을_때() {
        // given - 모든 계정 삭제
        memberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThat(memberRepository.existsByRole(Role.USER)).isFalse();
        assertThat(memberRepository.existsByRole(Role.ADMIN)).isFalse();
    }
}