package com.griotold.prompthub.domain.inquiry;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InquiryRegisterRequest(
        @NotNull(message = "문의 카테고리는 필수입니다")
        InquiryCategory inquiryCategory,

         @NotBlank(message = "제목은 필수입니다")
         @Size(max = 200, message = "제목은 200자 이하여야 합니다")
         String title,

         @NotBlank(message = "문의 내용은 필수입니다")
         @Size(max = 2000, message = "문의 내용은 2000자 이하여야 합니다")
         String content,

         @Email(message = "올바른 이메일 형식이어야 합니다")
         String contactEmail,

         @Size(max = 100, message = "이름은 100자 이하여야 합니다")
         String contactName
) {
}
