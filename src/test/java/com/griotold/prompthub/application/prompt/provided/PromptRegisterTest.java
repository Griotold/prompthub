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

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
record PromptRegisterTest(PromptRegister promptRegister,
                          PromptRepository promptRepository,
                          PromptLikeRepository promptLikeRepository,
                          EntityManager entityManager) {

    @Test
    void register() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        // when
        Prompt prompt = promptRegister.register(
                PromptFixture.createPromptRegisterRequest("새 프롬프트", "새 내용", "새 설명"),
                member, category
        );

        // then
        assertThat(prompt.getId()).isNotNull();
        assertThat(prompt.getTitle()).isEqualTo("새 프롬프트");
        assertThat(prompt.getContent()).isEqualTo("새 내용");
        assertThat(prompt.getIsPublic()).isTrue();
        assertThat(prompt.getLikesCount()).isEqualTo(0);
        assertThat(prompt.getViewsCount()).isEqualTo(0);
    }

    @Test
    void updateInfo() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("원래 제목", "원래 내용", member, category);

        // when
        Prompt updated = promptRegister.updateInfo(prompt.getId(),
                PromptFixture.createPromptUpdateRequest("수정된 제목", "수정된 내용", "수정된 설명"), member);

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void makePublic() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);
        prompt.makePrivate();
        promptRepository.save(prompt);

        // when
        Prompt publicPrompt = promptRegister.makePublic(prompt.getId(), member);

        // then
        assertThat(publicPrompt.getIsPublic()).isTrue();
    }

    @Test
    void makePrivate() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        // when
        Prompt privatePrompt = promptRegister.makePrivate(prompt.getId(), member);

        // then
        assertThat(privatePrompt.getIsPublic()).isFalse();
    }

    @Test
    void increaseViewCount() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        // when
        Prompt viewed = promptRegister.increaseViewCount(prompt.getId());

        // then
        assertThat(viewed.getViewsCount()).isEqualTo(1);
    }

    @Test
    void addLike() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        // when
        promptRegister.addLike(prompt.getId(), member);

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getLikesCount()).isEqualTo(1);
        assertThat(promptLikeRepository.existsByPromptAndMember(updated, member)).isTrue();
    }

    @Test
    void addLike_이미_좋아요한_경우() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        promptLikeRepository.save(PromptLike.create(member, prompt));

        // when & then
        assertThatThrownBy(() -> promptRegister.addLike(prompt.getId(), member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 좋아요한 프롬프트입니다.");
    }

    @Test
    void removeLike() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        promptLikeRepository.save(PromptLike.create(member, prompt));
        prompt.increaseLikeCount();
        promptRepository.save(prompt);

        // when
        promptRegister.removeLike(prompt.getId(), member);

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getLikesCount()).isEqualTo(0);
        assertThat(promptLikeRepository.existsByPromptAndMember(updated, member)).isFalse();
    }

    @Test
    void removeLike_좋아요하지_않은_경우() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        // when & then
        assertThatThrownBy(() -> promptRegister.removeLike(prompt.getId(), member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("좋아요하지 않은 프롬프트입니다.");
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