package com.griotold.prompthub.application.category;

import com.griotold.prompthub.application.category.provided.CategoryFinder;
import com.griotold.prompthub.application.category.provided.CategoryRegister;
import com.griotold.prompthub.application.category.provided.CategoryValidator;
import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryRegisterRequest;
import com.griotold.prompthub.domain.category.CategoryUpdateRequest;
import com.griotold.prompthub.domain.category.DuplicateCategoryNameException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class CategoryModifyService implements CategoryRegister {

    private final CategoryFinder categoryFinder;
    private final CategoryValidator categoryValidator;
    private final CategoryRepository categoryRepository;

    @Override
    public Category register(CategoryRegisterRequest request) {
        boolean isDuplicated = categoryValidator.existsByName(request.name());
        if (isDuplicated) {
            throw new DuplicateCategoryNameException("이미 존재하는 카테고리 이름입니다: " + request.name());
        }
        return categoryRepository.save(Category.register(request));
    }

    @Override
    public Category deactivate(Long categoryId) {
        Category category = categoryFinder.find(categoryId);
        category.deactivate();
        return categoryRepository.save(category);
    }

    @Override
    public Category activate(Long categoryId) {
        Category category = categoryFinder.find(categoryId);
        category.activate();
        return categoryRepository.save(category);
    }

    @Override
    public Category updateInfo(Long categoryId, CategoryUpdateRequest request) {
        boolean isDuplicated = categoryValidator.existsByNameExceptId(request.name(), categoryId);
        if (isDuplicated) {
            throw new DuplicateCategoryNameException("이미 존재하는 카테고리 이름입니다: " + request.name());
        }
        Category category = categoryFinder.find(categoryId);
        category.updateInfo(request);
        return categoryRepository.save(category);
    }
}
