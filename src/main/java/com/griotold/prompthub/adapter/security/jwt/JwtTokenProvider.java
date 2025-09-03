package com.griotold.prompthub.adapter.security.jwt;

import com.griotold.prompthub.domain.member.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    public JwtTokenProvider(
            @Value("${application.security.jwt.secret-key}") String secret,
            @Value("${application.security.jwt.expiration:3600000}") long accessTokenValidityTime, // 1시간
            @Value("${application.security.jwt.refresh-token.expiration:604800000}") long refreshTokenValidityTime // 7일
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenValidityTime = accessTokenValidityTime;
        this.refreshTokenValidityTime = refreshTokenValidityTime;
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long memberId, String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityTime);

        return Jwts.builder()
                .claim("memberId", memberId)
                .claim("email", email)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createAccessToken(Member member) {
        return createAccessToken(member.getId(), member.getEmail().address(), member.getRole().name());
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityTime);

        return Jwts.builder()
                .claim("memberId", memberId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Token에서 Claims 추출
     */
    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Token에서 memberId 추출
     */
    public Long getMemberId(String token) {
        Claims claims = getClaims(token);
        return claims.get("memberId", Long.class);
    }

    /**
     * Token에서 email 추출
     */
    public String getEmail(String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * Token에서 role 추출
     */
    public String getRole(String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Token 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token 타입 확인 (access/refresh)
     */
    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return claims.get("type", String.class);
    }

    /**
     * Token에서 username(email) 추출 - JwtAuthenticationFilter용
     */
    public String getUsername(String token) {
        return getEmail(token);  // 기존 getEmail() 재사용
    }

    /**
     * Token 만료 여부 확인 - JwtAuthenticationFilter용
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.debug("Token expiration check failed: {}", e.getMessage());
            return true;  // 예외 발생 시 만료된 것으로 처리
        }
    }

    public boolean isValidRefreshToken(String refreshToken) {
        try {
            return !isExpired(refreshToken);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshTokenMatchUser(String refreshToken, Long memberId) {
        try {
            Long currentMemberId = getMemberIdFromRefreshToken(refreshToken);
            return memberId.equals(currentMemberId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 리프레시 토큰에서 사용자명 추출
     */
    public Long getMemberIdFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .get("memberId", Long.class);
    }
}
