package com.griotold.prompthub.adapter.webapi.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 네이버는 state도 같이 넘겨줘야 함.
 * */
public record NaverLoginRequest(
        @NotBlank(message = "인가코드는 필수 입니다.")
        String authorizationCode,
        @NotBlank(message = "state 는 필수 입니다.")
        String state
) {
}
