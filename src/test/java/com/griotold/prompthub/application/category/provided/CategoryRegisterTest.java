package com.griotold.prompthub.application.category.provided;

import com.griotold.prompthub.application.category.required.CategoryRepository;
import com.griotold.prompthub.domain.category.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
record CategoryRegisterTest(CategoryRegister categoryRegister,
                            CategoryFinder categoryFinder,
                            CategoryRepository categoryRepository,
                            EntityManager entityManager) {

    @Test
    void register() {
        CategoryRegisterRequest request = new CategoryRegisterRequest("콘텐츠 작성", "설명");

        Category saved = categoryRegister.register(request);
        entityManager.flush();
        entityManager.clear();

        Category found = categoryFinder.find(saved.getId());
        assertThat(found.getName()).isEqualTo("콘텐츠 작성");
        assertThat(found.getDescription()).isEqualTo("설명");
        assertThat(found.getIsActive()).isTrue();
    }

    @Test
    void register_중복_예외() {
        categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "설명"));
        entityManager.flush();
        entityManager.clear();

        CategoryRegisterRequest request = new CategoryRegisterRequest("콘텐츠 작성", "설명");
        assertThatThrownBy(() -> categoryRegister.register(request))
                .isInstanceOf(DuplicateCategoryNameException.class);
    }

    @Test
    void deactivate() {
        Category category = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "설명"));
        assertThat(category.getIsActive()).isTrue();
        entityManager.flush();
        entityManager.clear();

        Category deactivated = categoryRegister.deactivate(category.getId());
        assertThat(deactivated.getIsActive()).isFalse();
    }

    @Test
    void activate() {
        Category category = CategoryFixture.createCategory("콘텐츠 작성", "설명");
        category.deactivate();
        assertThat(category.getIsActive()).isFalse();

        category = categoryRepository.save(category);
        entityManager.flush();
        entityManager.clear();

        Category activated = categoryRegister.activate(category.getId());
        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    void updateInfo() {
        Category category = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "설명"));
        entityManager.flush();
        entityManager.clear();

        CategoryUpdateRequest request = new CategoryUpdateRequest("새 이름", "새 설명");
        Category updated = categoryRegister.updateInfo(category.getId(), request);
        assertThat(updated.getName()).isEqualTo("새 이름");
        assertThat(updated.getDescription()).isEqualTo("새 설명");
    }

    @Test
    void updateInfo_자기자신은_중복_예외_안터짐() {
        Category c1 = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "설명"));
        entityManager.flush();
        entityManager.clear();

        CategoryUpdateRequest request1 = new CategoryUpdateRequest("콘텐츠 작성", "수정된 설명");
        categoryRegister.updateInfo(c1.getId(), request1);
    }

    @Test
    void updateInfo_중복_예외() {
        Category c1 = categoryRepository.save(CategoryFixture.createCategory("콘텐츠 작성", "설명"));
        Category c2 = categoryRepository.save(CategoryFixture.createCategory("다른 이름", "설명"));
        entityManager.flush();
        entityManager.clear();


        CategoryUpdateRequest request = new CategoryUpdateRequest("콘텐츠 작성", "새 설명");
        assertThatThrownBy(() -> categoryRegister.updateInfo(c2.getId(), request))
                .isInstanceOf(DuplicateCategoryNameException.class);
    }
}
