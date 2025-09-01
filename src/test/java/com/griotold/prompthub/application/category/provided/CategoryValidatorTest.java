package com.griotold.prompthub.application.category.provided;

import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
record CategoryValidatorTest(CategoryValidator categoryValidator,
                             CategoryRepository categoryRepository,
                             EntityManager entityManager) {

    @Test
    void existsByName_이름이_이미_존재하면_true() {
        Category category = categoryRepository.save(
                CategoryFixture.createCategory("콘텐츠 작성", "설명")
        );
        entityManager.flush();
        entityManager.clear();

        boolean exists = categoryValidator.existsByName("콘텐츠 작성");
        assertThat(exists).isTrue();
    }

    @Test
    void existsByName_이름이_없으면_false() {
        boolean exists = categoryValidator.existsByName("없는 이름");
        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameExceptId_같은_이름이_자기자신이면_false() {
        Category category = categoryRepository.save(
                CategoryFixture.createCategory("콘텐츠 작성", "설명")
        );
        entityManager.flush();
        entityManager.clear();

        boolean exists = categoryValidator.existsByNameExceptId("콘텐츠 작성", category.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameExceptId_같은_이름이_다른_엔티티면_true() {
        Category c1 = categoryRepository.save(
                CategoryFixture.createCategory("콘텐츠 작성", "설명")
        );
        Category c2 = categoryRepository.save(
                CategoryFixture.createCategory("다른 이름", "다른 설명")
        );
        entityManager.flush();
        entityManager.clear();

        boolean exists = categoryValidator.existsByNameExceptId("콘텐츠 작성", c2.getId());
        assertThat(exists).isTrue();
    }
}
