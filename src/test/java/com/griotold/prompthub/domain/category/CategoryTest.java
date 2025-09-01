package com.griotold.prompthub.domain.category;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.createCategory("콘텐츠 작성", "블로그, 기사, SNS 포스팅을 위한 프롬프트");
    }

    @Test
    void create() {
        assertThat(category.getIsActive()).isTrue();
        assertThat(category.getName()).isEqualTo("콘텐츠 작성");
    }

    @Test
    void deactivate() {
        assertThat(category.getIsActive()).isTrue();

        category.deactivate();

        assertThat(category.getIsActive()).isFalse();
    }

    @Test
    void activate() {
        assertThat(category.getIsActive()).isTrue();

        category.deactivate();

        assertThat(category.getIsActive()).isFalse();

        category.activate();

        assertThat(category.getIsActive()).isTrue();
    }

    @Test
    void updateInfo() {
        assertThat(category.getName()).isEqualTo("콘텐츠 작성");

        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("업무 자동화", "이메일, 보고서, 회의록 작성을 위한 프롬프트");

        category.updateInfo(updateRequest);

        assertThat(category.getName()).isEqualTo(updateRequest.name());
        assertThat(category.getDescription()).isEqualTo(updateRequest.description());
    }

}