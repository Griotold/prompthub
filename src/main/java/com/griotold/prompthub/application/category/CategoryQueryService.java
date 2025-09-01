package com.griotold.prompthub.application.category;

import com.griotold.prompthub.application.category.provided.CategoryFinder;
import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.domain.category.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class CategoryQueryService implements CategoryFinder {

    private final CategoryRepository categoryRepository;

    @Override
    public Category find(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. id: " + categoryId));
    }

    @Override
    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다. name: " + name));
    }

    @Override
    public List<Category> findActiveOrderByName() {
        return categoryRepository.findByIsActiveTrueOrderByName();
    }
}
