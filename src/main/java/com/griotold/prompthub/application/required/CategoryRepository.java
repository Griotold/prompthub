package com.griotold.prompthub.application.required;

import com.griotold.prompthub.domain.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 활성 카테고리만 조회 (프론트 드롭다운용)
    List<Category> findByIsActiveTrueOrderByName();

    // 이름으로 조회
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
