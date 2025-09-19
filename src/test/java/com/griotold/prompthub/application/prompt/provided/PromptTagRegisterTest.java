package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.application.prompt.required.PromptTagRepository;
import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.prompt.PromptTag;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagFixture;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
@RequiredArgsConstructor
class PromptTagRegisterTest {
    final PromptTagRegister promptTagRegister;
    final PromptTagRepository promptTagRepository;
    final TagRepository tagRepository;
    final EntityManager entityManager;

    Category category;
    Member author;
    Prompt prompt1;
    Prompt prompt2;

    @BeforeEach
    void setUp() {
        category = CategoryFixture.createCategory("AI", "AI 카테고리");
        author = MemberFixture.createGoogleMember("author@prompthub.app", "작성자");
        prompt1 = PromptFixture.createPrompt(author, category);
        prompt2 = PromptFixture.createAnotherPrompt(author, category);

        entityManager.persist(category);
        entityManager.persist(author);
        entityManager.persist(prompt1);
        entityManager.persist(prompt2);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void linkTagsByNames_새로운_태그들_연결() {
        // given
        List<String> tagNames = List.of("Spring", "JPA", "MySQL");

        // when
        promptTagRegister.linkTagsByNames(prompt1, tagNames);
        entityManager.flush();
        entityManager.clear();

        // then
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(3);
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL");

        // 태그들이 실제로 생성되었는지 확인
        List<Tag> createdTags = tagRepository.findByNameIn(tagNames);
        assertThat(createdTags).hasSize(3);
    }

    @Test
    void linkTagsByNames_일부_기존_태그_포함() {
        // given
        tagRepository.save(TagFixture.createTag("Spring"));
        tagRepository.save(TagFixture.createTag("JPA"));
        entityManager.flush();
        entityManager.clear();

        List<String> tagNames = List.of("Spring", "JPA", "MySQL", "Redis");

        // when
        promptTagRegister.linkTagsByNames(prompt1, tagNames);
        entityManager.flush();
        entityManager.clear();

        // then
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(4);
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL", "Redis");

        // 총 4개 태그가 DB에 있어야 함 (기존 2개 + 새로 생성 2개)
        assertThat(tagRepository.count()).isEqualTo(4);
    }

    @Test
    void linkTagsByNames_중복_연결_방지() {
        // given
        List<String> firstTags = List.of("Spring", "JPA");
        promptTagRegister.linkTagsByNames(prompt1, firstTags);
        entityManager.flush();

        // when - 일부 중복된 태그들로 다시 연결 시도
        List<String> secondTags = List.of("Spring", "MySQL"); // Spring 중복
        promptTagRegister.linkTagsByNames(prompt1, secondTags);
        entityManager.flush();
        entityManager.clear();

        // then - 중복 연결 방지되어야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(3); // Spring(1) + JPA(1) + MySQL(1)
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL");
    }

    @Test
    void linkTagsByNames_null_입력() {
        // when
        promptTagRegister.linkTagsByNames(prompt1, null);
        entityManager.flush();

        // then - 아무것도 연결되지 않아야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).isEmpty();
    }

    @Test
    void linkTagsByNames_빈리스트_입력() {
        // when
        promptTagRegister.linkTagsByNames(prompt1, List.of());
        entityManager.flush();

        // then - 아무것도 연결되지 않아야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).isEmpty();
    }

    @Test
    void linkTagsByNames_중복_태그명_입력시_중복제거() {
        // given - 중복된 태그명 입력
        List<String> tagNames = List.of("Spring", "JPA", "Spring", "MySQL", "JPA");

        // when
        promptTagRegister.linkTagsByNames(prompt1, tagNames);
        entityManager.flush();
        entityManager.clear();

        // then - 중복 제거되어 3개만 연결되어야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(3);
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL");

        // 태그도 3개만 생성되어야 함
        assertThat(tagRepository.count()).isEqualTo(3);
    }

    @Test
    void linkTagsByNames_다른_프롬프트에는_영향없음() {
        // given
        List<String> tagNames = List.of("Spring", "JPA");

        // prompt2 초기 상태 확인
        List<PromptTag> prompt2TagsBefore = promptTagRepository.findByPromptWithTag(prompt2);
        assertThat(prompt2TagsBefore).isEmpty();

        // when - prompt1에만 태그 연결
        promptTagRegister.linkTagsByNames(prompt1, tagNames);
        entityManager.flush();
        entityManager.clear();

        // then - prompt1만 변경되고 prompt2는 영향 없음
        List<PromptTag> prompt1TagsAfter = promptTagRepository.findByPromptWithTag(prompt1);
        List<PromptTag> prompt2TagsAfter = promptTagRepository.findByPromptWithTag(prompt2);

        assertThat(prompt1TagsAfter).hasSize(2);
        assertThat(prompt1TagsAfter).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Spring", "JPA");

        assertThat(prompt2TagsAfter).isEmpty(); // 여전히 빈 상태
    }

    @Test
    void updateTagsByNames_기존_태그들을_새로운_태그들로_교체() {
        // given - 기존 태그들 연결
        List<String> originalTags = List.of("Spring", "JPA", "MySQL");
        promptTagRegister.linkTagsByNames(prompt1, originalTags);
        entityManager.flush();

        // when - 새로운 태그들로 업데이트
        List<String> newTags = List.of("React", "TypeScript", "Node.js");
        promptTagRegister.updateTagsByNames(prompt1, newTags);
        entityManager.flush();
        entityManager.clear();

        // then - 기존 태그는 없어지고 새 태그들만 연결되어야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(3);
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("React", "TypeScript", "Node.js");

        // 기존 태그들이 연결 해제되었는지 확인
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .doesNotContain("Spring", "JPA", "MySQL");
    }

    @Test
    void updateTagsByNames_기존_태그들을_빈리스트로_업데이트() {
        // given - 기존 태그들 연결
        List<String> originalTags = List.of("Spring", "JPA");
        promptTagRegister.linkTagsByNames(prompt1, originalTags);
        entityManager.flush();

        // when - 빈 리스트로 업데이트 (모든 태그 해제)
        promptTagRegister.updateTagsByNames(prompt1, List.of());
        entityManager.flush();
        entityManager.clear();

        // then - 모든 태그가 해제되어야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).isEmpty();
    }

    @Test
    void updateTagsByNames_태그없는_프롬프트에_새로운_태그들_연결() {
        // given - 태그가 없는 상태 확인
        List<PromptTag> initialTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(initialTags).isEmpty();

        // when - 새로운 태그들 연결
        List<String> newTags = List.of("Vue", "Vuex", "Nuxt");
        promptTagRegister.updateTagsByNames(prompt1, newTags);
        entityManager.flush();
        entityManager.clear();

        // then - 새 태그들이 연결되어야 함
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(promptTags).hasSize(3);
        assertThat(promptTags).extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("Vue", "Vuex", "Nuxt");
    }

    @Test
    void unlinkAllTags_연결된_모든_태그_해제() {
        // given - 태그들 연결
        List<String> tagNames = List.of("Spring", "JPA", "Redis", "Docker");
        promptTagRegister.linkTagsByNames(prompt1, tagNames);
        entityManager.flush();

        // 연결 상태 확인
        List<PromptTag> beforeUnlink = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(beforeUnlink).hasSize(4);

        // when - 모든 태그 연결 해제
        promptTagRegister.unlinkAllTags(prompt1);
        entityManager.flush();
        entityManager.clear();

        // then - 모든 태그가 해제되어야 함
        List<PromptTag> afterUnlink = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(afterUnlink).isEmpty();

        // 태그 자체는 여전히 존재해야 함 (다른 프롬프트가 사용할 수 있음)
        assertThat(tagRepository.count()).isEqualTo(4);
    }

    @Test
    void unlinkAllTags_태그없는_프롬프트에_호출해도_문제없음() {
        // given - 태그가 없는 상태 확인
        List<PromptTag> beforeUnlink = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(beforeUnlink).isEmpty();

        // when - 태그가 없는 프롬프트에 unlinkAllTags 호출
        promptTagRegister.unlinkAllTags(prompt1);
        entityManager.flush();
        entityManager.clear();

        // then - 여전히 빈 상태여야 하고 예외 발생하지 않아야 함
        List<PromptTag> afterUnlink = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(afterUnlink).isEmpty();
    }
}