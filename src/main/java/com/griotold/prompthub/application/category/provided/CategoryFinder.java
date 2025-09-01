package com.griotold.prompthub.application.category.provided;

import com.griotold.prompthub.domain.category.Category;

import java.util.List;

/**
 * 카테고리를 조회한다.
 * */
public interface CategoryFinder {

    /** ID로 단건 조회(없으면 예외) */
    Category find(Long categoryId);

    /** 이름으로 단건 조회(없으면 예외) */
    Category findByName(String name);

    /** 활성 카테고리만 이름순 조회(드롭다운 등 용도) */
    List<Category> findActiveOrderByName();
}
