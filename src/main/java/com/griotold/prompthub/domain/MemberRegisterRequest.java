package com.griotold.prompthub.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberRegisterRequest(

        @Email String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8-20자 사이여야 합니다")
        String password,

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        String passwordCheck,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 5, max = 20, message = "닉네임은 20자 이하로 입력해주세요")
        String nickName

) {
}
