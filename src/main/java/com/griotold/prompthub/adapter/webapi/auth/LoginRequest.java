package com.griotold.prompthub.adapter.webapi.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 인가 코드 - 구글, 네이버, 카카오 공통
 * */
public record LoginRequest(
        @NotBlank(message = "인가코드는 필수입니다.")
        String authorizationCode
) {
}
