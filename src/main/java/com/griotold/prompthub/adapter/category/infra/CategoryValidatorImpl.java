package com.griotold.prompthub.adapter.category.infra;

import com.griotold.prompthub.application.category.provided.CategoryValidator;
import com.griotold.prompthub.application.category.required.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryValidatorImpl implements CategoryValidator {

    private final CategoryRepository categoryRepository;

    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameExceptId(String name, Long excludedId) {
        return categoryRepository.existsByNameAndIdNot(name, excludedId);
    }
}
