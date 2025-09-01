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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
class PromptRepositoryTest {

    @Autowired
    PromptRepository promptRepository;

    @Autowired
    EntityManager entityManager;

    Prompt prompt;
    Member member;
    Category category;

    @BeforeEach
    void setUp() {
        member = Member.register(
                MemberFixture.createMemberRegisterRequest("test@test.com", "password123", "password123", "testnick"),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(member);

        category = CategoryFixture.createCategory("콘텐츠 작성", "블로그용 프롬프트");
        entityManager.persist(category);

        prompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("테스트 제목", "테스트 내용", "테스트 설명"),
                member, category
        );
        promptRepository.save(prompt);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByIsPublicTrueOrderByCreatedAtDesc() {
        Prompt privatePrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("비공개 제목", "비공개 내용", "비공개 설명"),
                member, category
        );
        privatePrompt.makePrivate();
        promptRepository.save(privatePrompt);

        Page<Prompt> publicPrompts = promptRepository.findByIsPublicTrueOrderByCreatedAtDesc(PageRequest.of(0, 10));

        assertThat(publicPrompts.getContent()).hasSize(1);
        assertThat(publicPrompts.getContent().getFirst().getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    void findByCategoryAndIsPublicTrueOrderByCreatedAtDesc() {
        Category anotherCategory = CategoryFixture.createCategory("업무 자동화", "업무용 프롬프트");
        entityManager.persist(anotherCategory);

        Prompt anotherPrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("다른 카테고리", "다른 내용", "다른 설명"),
                member, anotherCategory
        );
        promptRepository.save(anotherPrompt);

        Page<Prompt> contentPrompts = promptRepository.findByCategoryAndIsPublicTrueOrderByCreatedAtDesc(
                category, PageRequest.of(0, 10)
        );

        assertThat(contentPrompts.getContent()).hasSize(1);
        assertThat(contentPrompts.getContent().getFirst().getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    void findByMemberOrderByCreatedAtDesc() {
        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(anotherMember);

        Prompt anotherPrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("다른 사용자", "다른 내용", "다른 설명"),
                anotherMember, category
        );
        promptRepository.save(anotherPrompt);

        Page<Prompt> memberPrompts = promptRepository.findByMemberOrderByCreatedAtDesc(
                member, PageRequest.of(0, 10)
        );

        assertThat(memberPrompts.getContent()).hasSize(1);
        assertThat(memberPrompts.getContent().getFirst().getTitle()).isEqualTo("테스트 제목");
    }

    @Test
    void promptLike_추가_및_삭제() {
        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(anotherMember);
        entityManager.flush();

        // 좋아요 추가
        prompt.addLike(anotherMember);
        promptRepository.save(prompt);
        entityManager.flush();
        entityManager.clear();

        // 검증: EntityManager로 직접 쿼리
        List<PromptLike> likes = entityManager.createQuery(
                        "SELECT pl FROM PromptLike pl WHERE pl.prompt.id = :promptId", PromptLike.class)
                .setParameter("promptId", prompt.getId())
                .getResultList();

        assertThat(likes).hasSize(1);
        assertThat(likes.get(0).getMember().getNickname()).isEqualTo("anothernick");

        // 좋아요 삭제
        Prompt foundPrompt = promptRepository.findById(prompt.getId()).get();
        foundPrompt.removeLike(anotherMember);
        promptRepository.save(foundPrompt);
        entityManager.flush();
        entityManager.clear();

        // 삭제 검증
        List<PromptLike> likesAfterRemove = entityManager.createQuery(
                        "SELECT pl FROM PromptLike pl WHERE pl.prompt.id = :promptId", PromptLike.class)
                .setParameter("promptId", prompt.getId())
                .getResultList();

        assertThat(likesAfterRemove).isEmpty();
    }
}