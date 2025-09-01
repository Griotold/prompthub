package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PromptTest {

    Prompt prompt;
    Member member;
    Category category;

    @BeforeEach
    void setUp() {
        prompt = PromptFixture.createPrompt("테스트 제목", "테스트 내용", "테스트 설명");
        member = prompt.getMember();
        category = prompt.getCategory();
    }

    @Test
    void create() {
        assertThat(prompt.getTitle()).isEqualTo("테스트 제목");
        assertThat(prompt.getContent()).isEqualTo("테스트 내용");
        assertThat(prompt.getDescription()).isEqualTo("테스트 설명");
        assertThat(prompt.getMember()).isNotNull();
        assertThat(prompt.getCategory()).isNotNull();
        assertThat(prompt.getViewsCount()).isEqualTo(0);
        assertThat(prompt.getLikesCount()).isEqualTo(0);
        assertThat(prompt.getIsPublic()).isTrue();
    }

    @Test
    void makePrivate() {
        assertThat(prompt.getIsPublic()).isTrue();

        prompt.makePrivate();

        assertThat(prompt.getIsPublic()).isFalse();
    }

    @Test
    void makePublic() {
        prompt.makePrivate();
        assertThat(prompt.getIsPublic()).isFalse();

        prompt.makePublic();

        assertThat(prompt.getIsPublic()).isTrue();
    }

    @Test
    void updateInfo() {
        PromptUpdateRequest updateRequest = PromptFixture.createPromptUpdateRequest("수정된 제목", "수정된 내용", "수정된 설명");

        prompt.updateInfo(updateRequest);

        assertThat(prompt.getTitle()).isEqualTo("수정된 제목");
        assertThat(prompt.getContent()).isEqualTo("수정된 내용");
        assertThat(prompt.getDescription()).isEqualTo("수정된 설명");
    }

    @Test
    void increaseViewCount() {
        assertThat(prompt.getViewsCount()).isEqualTo(0);

        prompt.increaseViewCount();

        assertThat(prompt.getViewsCount()).isEqualTo(1);

        prompt.increaseViewCount();

        assertThat(prompt.getViewsCount()).isEqualTo(2);
    }

    @Test
    void isOwnedBy() {
        assertThat(prompt.isOwnedBy(member)).isTrue();

        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );

        assertThat(prompt.isOwnedBy(anotherMember)).isFalse();
    }

    @Test
    void changeCategory() {
        Category newCategory = CategoryFixture.createCategory("새로운 카테고리", "새로운 설명");

        prompt.changeCategory(newCategory);

        assertThat(prompt.getCategory()).isEqualTo(newCategory);
    }

    @Test
    void addLike() {
        assertThat(prompt.getLikesCount()).isEqualTo(0);
        assertThat(prompt.getPromptLikes()).isEmpty();

        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );

        PromptLike promptLike = prompt.addLike(anotherMember);

        assertThat(prompt.getLikesCount()).isEqualTo(1);
        assertThat(prompt.getPromptLikes()).hasSize(1);
        assertThat(promptLike.getMember()).isEqualTo(anotherMember);
        assertThat(promptLike.getPrompt()).isEqualTo(prompt);
    }

    @Test
    void removeLike() {
        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );

        prompt.addLike(anotherMember);
        assertThat(prompt.getLikesCount()).isEqualTo(1);
        assertThat(prompt.getPromptLikes()).hasSize(1);

        prompt.removeLike(anotherMember);

        assertThat(prompt.getLikesCount()).isEqualTo(0);
        assertThat(prompt.getPromptLikes()).isEmpty();
    }

    @Test
    void removeLike_존재하지_않는_좋아요() {
        Member anotherMember = Member.register(
                MemberFixture.createMemberRegisterRequest("another@test.com", "password123", "password123", "anothernick"),
                MemberFixture.createPasswordEncoder()
        );

        assertThatThrownBy(() -> prompt.removeLike(anotherMember))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("좋아요가 존재하지 않습니다");
    }

    @Test
    void increaseLikeCount() {
        assertThat(prompt.getLikesCount()).isEqualTo(0);

        prompt.increaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(1);
    }

    @Test
    void decreaseLikeCount() {
        prompt.increaseLikeCount();
        assertThat(prompt.getLikesCount()).isEqualTo(1);

        prompt.decreaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(0);
    }

    @Test
    void decreaseLikeCount_0이하로_내려가지_않음() {
        assertThat(prompt.getLikesCount()).isEqualTo(0);

        prompt.decreaseLikeCount();

        assertThat(prompt.getLikesCount()).isEqualTo(0);
    }
}