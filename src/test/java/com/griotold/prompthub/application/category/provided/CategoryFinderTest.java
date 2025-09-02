package com.griotold.prompthub.application.category.provided;

import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
record CategoryFinderTest(CategoryFinder categoryFinder,
                          CategoryRepository categoryRepository,
                          EntityManager entityManager) {

    @Test
    void find() {
        Category category = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "그에 따른 설명"));
        entityManager.flush();
        entityManager.clear();

        Category found = categoryFinder.find(category.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getName()).isEqualTo("콘텐츠 작성");
        assertThat(found.getDescription()).isEqualTo("그에 따른 설명");
    }

    @Test
    void find_없는_id일때() {
        assertThatThrownBy(() -> categoryFinder.find(999L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findByName() {
        Category category = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "그에 따른 설명"));
        entityManager.flush();
        entityManager.clear();

        Category found = categoryFinder.findByName("콘텐츠 작성");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getName()).isEqualTo("콘텐츠 작성");
        assertThat(found.getDescription()).isEqualTo("그에 따른 설명");
    }

    @Test
    void findByName_없는_name일때() {
        assertThatThrownBy(() -> categoryFinder.findByName("없는 이름"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findActiveOrderByName() {
        Category category = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "그에 따른 설명"));
        entityManager.flush();
        entityManager.clear();

        Category category2 = CategoryFixture.createCategory("비활성화", "비활성화");
        category2.deactivate();
        assertThat(category2.getIsActive()).isFalse();
        categoryRepository.save(category2);
        entityManager.flush();
        entityManager.clear();

        List<Category> found = categoryFinder.findActiveOrderByName();
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getName()).isEqualTo("콘텐츠 작성");
        assertThat(found.getFirst().getIsActive()).isTrue();
    }
}