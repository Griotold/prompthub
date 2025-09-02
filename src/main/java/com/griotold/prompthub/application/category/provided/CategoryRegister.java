package com.griotold.prompthub.application.category.provided;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryRegisterRequest;
import com.griotold.prompthub.domain.category.CategoryUpdateRequest;
import jakarta.validation.Valid;

public interface CategoryRegister {

    /** 새 카테고리 등록 */
    Category register(@Valid CategoryRegisterRequest request);

    /** 카테고리 비활성화 */
    Category deactivate(Long categoryId);

    /** 카테고리 활성화 */
    Category activate(Long categoryId);

    /** 카테고리 이름/설명 수정 */
    Category updateInfo(Long categoryId, @Valid CategoryUpdateRequest request);
}

