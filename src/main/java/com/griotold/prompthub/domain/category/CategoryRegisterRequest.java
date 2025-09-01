package com.griotold.prompthub.domain.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRegisterRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다")
        @Size(max = 50, message = "카테고리 이름 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "카테고리 설명은 필수입니다")
        @Size(max = 200, message = "카테고리 설명 200자 이하여야 합니다.")
        String description
) {
}
