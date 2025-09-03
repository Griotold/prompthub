package com.griotold.prompthub.adapter.webapi.dto.response;

import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.domain.member.Member;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {

    /**
     * Member와 JwtTokenProvider를 받아 RefreshTokenResponse 생성
     */
    public static RefreshTokenResponse fromMember(Member member, JwtTokenProvider jwtTokenProvider) {
        String newAccessToken = jwtTokenProvider.createAccessToken(member);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        return new RefreshTokenResponse(newAccessToken, newRefreshToken);
    }
}
