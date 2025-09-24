package com.griotold.prompthub.domain.prompt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PromptRegisterRequest(

        @NotBlank(message = "프롬프트 제목은 필수입니다")
        @Size(max = 200, message = "프롬프트 제목은 200자 이하여야 합니다")
        String title,

        @NotBlank(message = "프롬프트 내용은 필수입니다")
        @Size(max = 5000, message = "프롬프트 내용은 5000자 이하여야 합니다")
        String content,

        @Size(max = 1000, message = "프롬프트 설명은 1000자 이하여야 합니다")
        String description,

        @NotNull(message = "카테고리는 필수입니다")
        Long categoryId,

        @Size(max = 10, message = "태그는 최대 10개까지 가능합니다")
        List<String> tags
) {

        /**
         * 처리할 유효한 태그가 있는지 확인
         * @return 유효한 태그가 하나라도 있으면 true
         */
        public boolean hasValidTags() {
                return tags != null && !tags.isEmpty() &&
                        tags.stream().anyMatch(tag -> tag != null && !tag.trim().isEmpty());
        }

        /**
         * 유효한 태그 목록 반환 (null, 빈 문자열, 공백 제거 및 trim 처리)
         * @return 정제된 유효한 태그 리스트 (빈 리스트일 수 있음)
         */
        public List<String> getValidTags() {
                if (tags == null) {
                        return List.of();
                }
                return tags.stream()
                        .filter(tag -> tag != null && !tag.trim().isEmpty())
                        .map(String::trim)
                        .distinct() // 중복 태그 제거
                        .toList();
        }
}
