package com.griotold.prompthub.application.required;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberStatus;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager entityManager;

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

}