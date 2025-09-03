package com.griotold.prompthub.adapter.security.social;

import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.domain.member.Member;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean isNewMember
) {
    /**
     * Member와 신규 여부를 받아 TokenResponse 생성
     */
    public static TokenResponse fromMember(Member member, boolean isNewMember, JwtTokenProvider jwtTokenProvider) {
        String accessToken = jwtTokenProvider.createAccessToken(member);
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        return new TokenResponse(accessToken, refreshToken, isNewMember);
    }
}
