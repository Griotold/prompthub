package com.griotold.prompthub.adapter.security.social;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        boolean isNewMember
) {}
