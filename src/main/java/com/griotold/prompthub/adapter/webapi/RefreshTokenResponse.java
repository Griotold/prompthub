package com.griotold.prompthub.adapter.webapi;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
}
