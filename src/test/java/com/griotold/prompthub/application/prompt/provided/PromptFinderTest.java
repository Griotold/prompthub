package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.prompt.PromptLike;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
record PromptFinderTest(PromptFinder promptFinder,
                        PromptRepository promptRepository,
                        PromptLikeRepository promptLikeRepository,
                        EntityManager entityManager) {

    @Test
    void find() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트 제목", "테스트 내용", member, category);

        // when
        Prompt found = promptFinder.find(prompt.getId());

        // then
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("테스트 제목");
        assertThat(found.getContent()).isEqualTo("테스트 내용");
    }

    @Test
    void find_없는_id일때() {
        assertThatThrownBy(() -> promptFinder.find(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAllPublic() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        Prompt publicPrompt = createAndSavePrompt("공개 프롬프트", "공개 내용", member, category);

        Prompt privatePrompt = createAndSavePrompt("비공개 프롬프트", "비공개 내용", member, category);
        privatePrompt.makePrivate();
        promptRepository.save(privatePrompt);

        // when
        Page<Prompt> publicPrompts = promptFinder.findAllPublic(PageRequest.of(0, 10));

        // then
        assertThat(publicPrompts.getContent()).hasSize(1);
        assertThat(publicPrompts.getContent().getFirst().getTitle()).isEqualTo("공개 프롬프트");
    }

    @Test
    void findAllPublicByCategory() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category1 = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Category category2 = createAndSaveCategory("업무 자동화", "업무용 프롬프트");

        createAndSavePrompt("카테고리1 프롬프트", "내용", member, category1);
        createAndSavePrompt("카테고리2 프롬프트", "내용", member, category2);

        // when
        Page<Prompt> category1Prompts = promptFinder.findAllPublicByCategory(category1, PageRequest.of(0, 10));

        // then
        assertThat(category1Prompts.getContent()).hasSize(1);
        assertThat(category1Prompts.getContent().getFirst().getTitle()).isEqualTo("카테고리1 프롬프트");
    }

    @Test
    void findAllByMember() {
        // given
        Member member1 = createAndSaveMember("test1@test.com", "testnick1");
        Member member2 = createAndSaveMember("test2@test.com", "testnick2");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        createAndSavePrompt("회원1 프롬프트", "내용", member1, category);
        createAndSavePrompt("회원2 프롬프트", "내용", member2, category);

        // when
        Page<Prompt> member1Prompts = promptFinder.findAllByMember(member1, PageRequest.of(0, 10));

        // then
        assertThat(member1Prompts.getContent()).hasSize(1);
        assertThat(member1Prompts.getContent().getFirst().getTitle()).isEqualTo("회원1 프롬프트");
    }

    @Test
    void searchPublic() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        createAndSavePrompt("블로그 작성법", "내용", member, category);
        createAndSavePrompt("다른 제목", "블로그 관련 내용", member, category);
        createAndSavePrompt("관련없음", "관련없는 내용", member, category);

        // when
        Page<Prompt> searchResults = promptFinder.searchPublic("블로그", PageRequest.of(0, 10));

        // then
        assertThat(searchResults.getContent()).hasSize(2);
    }

    @Test
    void findPopular() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        Prompt normalPrompt = createAndSavePrompt("일반 프롬프트", "내용", member, category);

        Prompt popularPrompt = createAndSavePrompt("인기 프롬프트", "내용", member, category);
        popularPrompt.increaseLikeCount();
        popularPrompt.increaseLikeCount();
        promptRepository.save(popularPrompt);

        // when
        Page<Prompt> popularPrompts = promptFinder.findPopular(PageRequest.of(0, 10));

        // then
        assertThat(popularPrompts.getContent()).hasSize(2);
        assertThat(popularPrompts.getContent().getFirst().getTitle()).isEqualTo("인기 프롬프트");
        assertThat(popularPrompts.getContent().getFirst().getLikesCount()).isEqualTo(2);
    }

    @Test
    void isLikedBy_좋아요한_경우() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트 프롬프트", "내용", member, category);

        PromptLike promptLike = PromptLike.create(member, prompt);
        promptLikeRepository.save(promptLike);

        // when
        boolean isLiked = promptFinder.isLikedBy(prompt.getId(), member);

        // then
        assertThat(isLiked).isTrue();
    }

    @Test
    void isLikedBy_좋아요하지_않은_경우() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트 프롬프트", "내용", member, category);

        // when
        boolean isLiked = promptFinder.isLikedBy(prompt.getId(), member);

        // then
        assertThat(isLiked).isFalse();
    }

    private Member createAndSaveMember(String email, String nickname) {
        Member member = Member.register(
                MemberFixture.createMemberRegisterRequest(email, "password123", "password123", nickname),
                MemberFixture.createPasswordEncoder()
        );
        entityManager.persist(member);
        entityManager.flush();
        entityManager.clear();
        return member;
    }

    private Category createAndSaveCategory(String name, String description) {
        Category category = CategoryFixture.createCategory(name, description);
        entityManager.persist(category);
        entityManager.flush();
        entityManager.clear();
        return category;
    }

    private Prompt createAndSavePrompt(String title, String content, Member member, Category category) {
        Prompt prompt = Prompt.register(
                PromptFixture.createPromptRegisterRequest(title, content, "설명"),
                member, category
        );
        promptRepository.save(prompt);
        entityManager.flush();
        entityManager.clear();
        return prompt;
    }
}