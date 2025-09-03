package com.griotold.prompthub.adapter.security.social.google;

public record GoogleUserResponse(
        String sub,    // 구글 고유 ID
        String email,
        String name,
        String picture,
        Boolean emailVerified
) {}
