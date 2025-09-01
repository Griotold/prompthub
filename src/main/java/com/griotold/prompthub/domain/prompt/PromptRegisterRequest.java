package com.griotold.prompthub.domain.prompt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PromptRegisterRequest(

        @NotBlank(message = "프롬프트 제목은 필수입니다")
        @Size(max = 200, message = "프롬프트 제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "프롬프트 내용은 필수입니다")
        @Size(max = 5000, message = "프롬프트 내용은 5000자 이하여야 합니다")
        String content,

        @Size(max = 1000, message = "프롬프트 설명은 1000자 이하여야 합니다")
        String description
) {
}
