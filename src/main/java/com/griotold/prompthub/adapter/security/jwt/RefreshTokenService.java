package com.griotold.prompthub.adapter.security.jwt;

import com.griotold.prompthub.adapter.webapi.dto.response.RefreshTokenResponse;
import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberFinder memberFinder;

    /**
     * 리프레시 토큰으로 새로운 액세스 토큰 발급
     */
    public RefreshTokenResponse refreshToken(String refreshToken, Long memberId) {
        log.info("토큰 갱신 시작 - 사용자 ID : {}", memberId);

        assertValidRefreshToken(refreshToken);

        assertTokenOwnerMatches(refreshToken, memberId);

        Member member = memberFinder.find(memberId);

        log.info("토큰 갱신 완료 - 사용자: {}", member.getEmail().address());

        return RefreshTokenResponse.fromMember(member, jwtTokenProvider);
    }

    private void assertValidRefreshToken(String refreshToken) {
        if (!jwtTokenProvider.isValidRefreshToken(refreshToken)) {
            log.warn("유효하지 않은 리프레시 토큰: {}", refreshToken);
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private void assertTokenOwnerMatches(String refreshToken, Long memberId) {
        if (!jwtTokenProvider.isRefreshTokenMatchUser(refreshToken, memberId)) {
            log.warn("토큰 소유자 불일치 - 사용자 ID : {}", memberId);
            throw new IllegalArgumentException("토큰 소유자가 일치하지 않습니다.");
        }
    }
}

