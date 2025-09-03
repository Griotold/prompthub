package com.griotold.prompthub.adapter.webapi;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
