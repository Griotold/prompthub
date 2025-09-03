package com.griotold.prompthub.adapter.webapi.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
