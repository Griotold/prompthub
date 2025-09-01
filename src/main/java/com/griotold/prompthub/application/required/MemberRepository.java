package com.griotold.prompthub.application.required;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 기본 CRUD는 JpaRepository가 제공
    // 로그인용
    Optional<Member> findByEmail_Address(String emailAddress);
    Optional<Member> findByNickname(String nickname);

    // 중복 체크용 (회원가입시)
    boolean existsByEmail_Address(String emailAddress);
    boolean existsByNickname(String nickname);

    // 활성 회원만 조회
    List<Member> findByStatus(MemberStatus status);
}
