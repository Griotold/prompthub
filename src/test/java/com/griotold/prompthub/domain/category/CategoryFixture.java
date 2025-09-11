package com.griotold.prompthub.domain.category;

import org.springframework.test.util.ReflectionTestUtils;

public class CategoryFixture {

    public static CategoryRegisterRequest createCategoryRegisterRequest() {
        return new CategoryRegisterRequest("콘텐츠 작성", "블로그, 기사, SNS 포스팅을 위한 프롬프트");
    }

    public static CategoryRegisterRequest createCategoryRegisterRequest(String name, String description) {
        return new CategoryRegisterRequest(name, description);
    }

    public static CategoryUpdateRequest createCategoryUpdateRequest() {
        return new CategoryUpdateRequest("업무 자동화", "이메일, 보고서, 회의록 작성을 위한 프롬프트");
    }

    public static CategoryUpdateRequest createCategoryUpdateRequest(String name, String description) {
        return new CategoryUpdateRequest(name, description);
    }

    public static Category createCategory() {
        return Category.register(createCategoryRegisterRequest());
    }

    public static Category createCategory(String name, String description) {
        return Category.register(createCategoryRegisterRequest(name, description));
    }

    // id를 넣어서 엔티티를 리턴하는 메서드
    public static Category createCategory(Long id) {
        Category category = Category.register(createCategoryRegisterRequest());
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
