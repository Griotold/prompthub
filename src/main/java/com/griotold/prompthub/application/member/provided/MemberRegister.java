package com.griotold.prompthub.application.member.provided;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.SocialRegisterRequest;
import jakarta.validation.Valid;

public interface MemberRegister {
    // 소셜 로그인 회원가입
    Member registerWithSocial(@Valid SocialRegisterRequest registerRequest);

    // 비활성화된 계정 재활성화
    Member reactivate(Long memberId);
}