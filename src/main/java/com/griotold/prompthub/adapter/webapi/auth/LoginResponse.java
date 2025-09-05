package com.griotold.prompthub.adapter.webapi.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
