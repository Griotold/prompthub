package com.griotold.prompthub.application.category.provided;

public interface CategoryValidator {

    /** 이름 중복 여부 체크(등록검증용) */
    boolean existsByName(String name);

    /** 수정 시 자기 자신 제외 중복 체크 */
    boolean existsByNameExceptId(String name, Long excludedId);
}
