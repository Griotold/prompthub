package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

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
    void findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc_제목으로_검색() {
        // given
        Prompt titleMatchPrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("블로그 작성 프롬프트", "다른 내용", "설명"),
                member, category
        );
        promptRepository.save(titleMatchPrompt);

        // when
        Page<Prompt> searchResults = promptRepository.findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
                "블로그", "블로그", PageRequest.of(0, 10)
        );

        // then
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getContent().getFirst().getTitle()).contains("블로그");
    }

    @Test
    void findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc_내용으로_검색() {
        // given
        Prompt contentMatchPrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("다른 제목", "ChatGPT 프롬프트 내용", "설명"),
                member, category
        );
        promptRepository.save(contentMatchPrompt);

        // when
        Page<Prompt> searchResults = promptRepository.findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
                "ChatGPT", "ChatGPT", PageRequest.of(0, 10)
        );

        // then
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getContent().getFirst().getContent()).contains("ChatGPT");
    }

    @Test
    void findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc_비공개는_검색안됨() {
        // given
        Prompt privatePrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("검색 키워드", "내용", "설명"),
                member, category
        );
        privatePrompt.makePrivate();
        promptRepository.save(privatePrompt);

        // when
        Page<Prompt> searchResults = promptRepository.findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc(
                "검색", "검색", PageRequest.of(0, 10)
        );

        // then
        assertThat(searchResults.getContent()).isEmpty();
    }

    @Test
    void findByIsPublicTrueOrderByLikesCountDescCreatedAtDesc() {
        // given
        Prompt popularPrompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest("인기 프롬프트", "인기 내용", "설명"),
                member, category
        );
        // 좋아요 수 직접 증가 (테스트용)
        popularPrompt.increaseLikeCount();
        popularPrompt.increaseLikeCount();
        promptRepository.save(popularPrompt);

        // when
        Page<Prompt> popularPrompts = promptRepository.findByIsPublicTrueOrderByLikesCountDescCreatedAtDesc(PageRequest.of(0, 10));

        // then
        assertThat(popularPrompts.getContent()).hasSize(2);
        assertThat(popularPrompts.getContent().getFirst().getTitle()).isEqualTo("인기 프롬프트");
        assertThat(popularPrompts.getContent().getFirst().getLikesCount()).isEqualTo(2);
    }
}