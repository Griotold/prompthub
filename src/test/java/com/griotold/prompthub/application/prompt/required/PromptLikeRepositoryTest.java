package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.prompt.PromptLike;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class PromptLikeRepositoryTest {

    @Autowired
    PromptLikeRepository promptLikeRepository;

    @Autowired
    EntityManager entityManager;

    Prompt prompt;
    Member member;
    Member anotherMember;
    Category category;

    @BeforeEach
    void setUp() {
        member = Member.register(
                MemberFixture.createMemberRegisterRequest("test@test.com", "password123", "password123", "testnick"),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(member);

        anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(anotherMember);

        category = CategoryFixture.createCategory("콘텐츠 작성", "블로그용 프롬프트");
        entityManager.persist(category);

        prompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("테스트 제목", "테스트 내용", "테스트 설명"),
                member, category
        );
        entityManager.persist(prompt);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void existsByPromptAndMember_존재하는_경우() {
        // given
        PromptLike promptLike = PromptLike.create(member, prompt);
        promptLikeRepository.save(promptLike);

        // when
        boolean exists = promptLikeRepository.existsByPromptAndMember(prompt, member);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByPromptAndMember_존재하지_않는_경우() {
        // when
        boolean exists = promptLikeRepository.existsByPromptAndMember(prompt, member);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    void deleteByPromptAndMember() {
        // given
        PromptLike promptLike = PromptLike.create(member, prompt);
        promptLikeRepository.save(promptLike);

        assertThat(promptLikeRepository.existsByPromptAndMember(prompt, member)).isTrue();

        // when
        promptLikeRepository.deleteByPromptAndMember(prompt, member);

        // then
        assertThat(promptLikeRepository.existsByPromptAndMember(prompt, member)).isFalse();
    }

    @Test
    void deleteByPromptAndMember_존재하지_않는_경우() {
        // when & then
        assertThatCode(() -> promptLikeRepository.deleteByPromptAndMember(prompt, member))
                .doesNotThrowAnyException();
    }
}