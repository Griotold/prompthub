package com.griotold.prompthub.domain.prompt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PromptRegisterRequestTest {

    @Test
    @DisplayName("태그가 null인 경우 hasValidTags는 false를 반환한다")
    void hasValidTags_WithNullTags_ReturnsFalse() {
        // given
        PromptRegisterRequest request = new PromptRegisterRequest(
                "제목", "내용", "설명", 1L, null
        );

        // when & then
        assertThat(request.hasValidTags()).isFalse();
    }

    @Test
    @DisplayName("태그가 빈 리스트인 경우 hasValidTags는 false를 반환한다")
    void hasValidTags_WithEmptyTags_ReturnsFalse() {
        // given
        PromptRegisterRequest request = new PromptRegisterRequest(
                "제목", "내용", "설명", 1L, List.of()
        );

        // when & then
        assertThat(request.hasValidTags()).isFalse();
    }

    @Test
    @DisplayName("유효한 태그가 하나라도 있으면 hasValidTags는 true를 반환한다")
    void hasValidTags_WithValidTags_ReturnsTrue() {
        // given
        PromptRegisterRequest request = new PromptRegisterRequest(
                "제목", "내용", "설명", 1L, List.of("", "자바", "   ")
        );

        // when & then
        assertThat(request.hasValidTags()).isTrue();
    }

    @Test
    @DisplayName("모든 태그가 유효한 경우 hasValidTags는 true를 반환한다")
    void hasValidTags_WithAllValidTags_ReturnsTrue() {
        // given
        PromptRegisterRequest request = new PromptRegisterRequest(
                "제목", "내용", "설명", 1L, List.of("자바", "스프링", "JPA")
        );

        // when & then
        assertThat(request.hasValidTags()).isTrue();
    }

    /**
     * getValidTags()
     * */
    @Test
    @DisplayName("getValidTags는 null과 빈 문자열을 제거하고 trim과 중복제거를 수행한다")
    void getValidTags_ProcessesTagsProperly() {
        // given
        PromptRegisterRequest request = new PromptRegisterRequest(
                "제목", "내용", "설명", 1L,
                List.of("", "  자바  ", "스프링", "자바", "   ", "\t스프링\t")
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).containsExactly("자바", "스프링");
    }
}