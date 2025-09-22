package com.griotold.prompthub.adapter.webapi.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank(message = "닉네임은 필수입니다")
        String nickname,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {}
