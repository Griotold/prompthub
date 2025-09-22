package com.griotold.prompthub.application.member.required;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberStatus;
import com.griotold.prompthub.domain.member.Provider;
import com.griotold.prompthub.domain.member.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 로그인용
    Optional<Member> findByEmail_Address(String emailAddress);
    Optional<Member> findByNickname(String nickname);

    // 중복 체크용 (회원가입시)
    boolean existsByEmail_Address(String emailAddress);
    boolean existsByNickname(String nickname);

    // 활성 회원만 조회
    List<Member> findByStatus(MemberStatus status);

    // 소셜 로그인용
    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    // 관리자 계정이 있는지 체크
    boolean existsByRole(Role role);
}
