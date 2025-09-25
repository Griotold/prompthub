package com.griotold.prompthub.domain.prompt;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PromptUpdateRequestTest {

    @Test
    void 태그가_null인_경우_hasValidTags는_false를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", null
        );

        // when & then
        assertThat(request.hasValidTags()).isFalse();
    }

    @Test
    void 태그가_빈_리스트인_경우_hasValidTags는_false를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", List.of()
        );

        // when & then
        assertThat(request.hasValidTags()).isFalse();
    }

    @Test
    void 태그가_모두_빈_문자열이거나_공백인_경우_hasValidTags는_false를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", List.of("", "   ", "\t\n")
        );

        // when & then
        assertThat(request.hasValidTags()).isFalse();
    }

    @Test
    void 유효한_태그가_하나라도_있으면_hasValidTags는_true를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", List.of("", "자바", "   ")
        );

        // when & then
        assertThat(request.hasValidTags()).isTrue();
    }

    @Test
    void 모든_태그가_유효한_경우_hasValidTags는_true를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", List.of("자바", "스프링", "JPA")
        );

        // when & then
        assertThat(request.hasValidTags()).isTrue();
    }

    @Test
    void 태그가_null인_경우_getValidTags는_빈_리스트를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", null
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 태그가_빈_리스트인_경우_getValidTags는_빈_리스트를_반환한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", List.of()
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void getValidTags는_null과_빈_문자열을_제거하고_trim과_중복제거를_수행한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명",
                List.of("", "  자바  ", "스프링", "자바", "   ", "\t스프링\t")
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).containsExactly("자바", "스프링");
    }

    @Test
    void getValidTags는_공백만_있는_태그들을_필터링한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명",
                List.of("자바", "   ", "", "\t", "스프링")
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).containsExactly("자바", "스프링");
    }

    @Test
    void getValidTags는_태그_순서를_유지하되_중복은_제거한다() {
        // given
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명",
                List.of("스프링", "자바", "JPA", "자바", "스프링부트")
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).containsExactly("스프링", "자바", "JPA", "스프링부트");
    }

    @Test
    void getValidTags는_최대_10개까지의_태그를_처리한다() {
        // given
        List<String> tags = List.of(
                "태그1", "태그2", "태그3", "태그4", "태그5",
                "태그6", "태그7", "태그8", "태그9", "태그10"
        );
        PromptUpdateRequest request = new PromptUpdateRequest(
                "제목", "내용", "설명", tags
        );

        // when
        List<String> result = request.getValidTags();

        // then
        assertThat(result).hasSize(10);
        assertThat(result).containsExactlyElementsOf(tags);
    }
}