package com.griotold.prompthub.application.member.provided;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.Provider;

import java.util.Optional;

public interface MemberFinder {

    // JWT에서 memberId로 조회
    Member find(Long memberId);

    // 소셜 로그인 시 기존 계정 확인
    Optional<Member> findBySocial(Provider provider, String providerId);
}
