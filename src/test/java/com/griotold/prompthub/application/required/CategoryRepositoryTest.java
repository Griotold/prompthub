package com.griotold.prompthub.application.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EntityManager entityManager;

    Category category;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.createCategory("콘텐츠 작성", "블로그, 기사, SNS 포스팅을 위한 프롬프트");
        categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByIsActiveTrueOrderByName() {
        Category deactivatedCategory = CategoryFixture.createCategory("비활성 카테고리", "비활성화된 카테고리");
        deactivatedCategory.deactivate();
        categoryRepository.save(deactivatedCategory);

        Category anotherActiveCategory = CategoryFixture.createCategory("업무 자동화", "업무 관련 프롬프트");
        categoryRepository.save(anotherActiveCategory);

        List<Category> activeCategories = categoryRepository.findByIsActiveTrueOrderByName();

        assertThat(activeCategories).hasSize(2);
        assertThat(activeCategories.get(0).getName()).isEqualTo("업무 자동화"); // 이름순 정렬
        assertThat(activeCategories.get(1).getName()).isEqualTo("콘텐츠 작성");
    }

    @Test
    void findByName() {
        Optional<Category> found = categoryRepository.findByName("콘텐츠 작성");

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("블로그, 기사, SNS 포스팅을 위한 프롬프트");
    }

    @Test
    void findByName_존재하지_않는_이름() {
        Optional<Category> found = categoryRepository.findByName("존재하지않는카테고리");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByName() {
        boolean exists = categoryRepository.existsByName("콘텐츠 작성");
        boolean notExists = categoryRepository.existsByName("존재하지않는카테고리");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}