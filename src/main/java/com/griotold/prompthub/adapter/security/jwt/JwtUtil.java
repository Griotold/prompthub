package com.griotold.prompthub.adapter.security.jwt;

import com.griotold.prompthub.domain.member.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${application.security.jwt.secret-key}") String secret,
            @Value("${application.security.jwt.expiration}") long accessTokenExpiration,
            @Value("${application.security.jwt.refresh-token.expiration}") long refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    // Access Token 생성
    public String createAccessToken(String username, Role role) {
        return Jwts.builder()
                .claim("username", username)
                .claim("role", role.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }


    /**
     * 리프레시 토큰에서 사용자명 추출
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .get("username", String.class);
    }

    /**
     * 리프레시 토큰이 유효한지 검증 (만료 여부만 체크)
     */
    public boolean isValidRefreshToken(String refreshToken) {
        try {
            return !isExpired(refreshToken);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 리프레시 토큰의 사용자명이 현재 인증된 사용자와 일치하는지 확인
     */
    public boolean isRefreshTokenMatchUser(String refreshToken, String username) {
        try {
            String tokenUsername = getUsernameFromRefreshToken(refreshToken);
            return username.equals(tokenUsername);
        } catch (Exception e) {
            return false;
        }
    }
}
