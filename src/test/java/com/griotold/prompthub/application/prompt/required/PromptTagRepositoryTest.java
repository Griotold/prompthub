package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptFixture;
import com.griotold.prompthub.domain.prompt.PromptTag;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagFixture;
import com.griotold.prompthub.support.annotation.RepositoryTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@RepositoryTest
@RequiredArgsConstructor
class PromptTagRepositoryTest {

    final PromptTagRepository promptTagRepository;
    final EntityManager entityManager;

    Member member1, member2;
    Category category1, category2;
    Prompt prompt1, prompt2;
    Tag tag1, tag2;

    @BeforeEach
    void setUp() {
        // 서로 다른 회원 생성
        member1 = MemberFixture.createGoogleMember("user1@test.com", "사용자1");
        member2 = MemberFixture.createGoogleMember("user2@test.com", "사용자2");

        // 서로 다른 카테고리 생성 (유니크 제약조건 피하기)
        category1 = CategoryFixture.createCategory("카테고리1", "설명1");
        category2 = CategoryFixture.createCategory("카테고리2", "설명2");

        // 프롬프트들 생성
        prompt1 = PromptFixture.createPrompt("프롬프트1", "내용1", "설명1");
        prompt2 = PromptFixture.createPrompt("프롬프트2", "내용2", "설명2");

        // 태그들 생성
        tag1 = TagFixture.createTag("마케팅");
        tag2 = TagFixture.createTag("광고");

        // 기본 엔티티들 저장
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(category1);
        entityManager.persist(category2);

        // 프롬프트에 연관관계 설정 (ReflectionTestUtils 사용)
        ReflectionTestUtils.setField(prompt1, "member", member1);
        ReflectionTestUtils.setField(prompt1, "category", category1);
        ReflectionTestUtils.setField(prompt2, "member", member2);
        ReflectionTestUtils.setField(prompt2, "category", category2);

        entityManager.persist(prompt1);
        entityManager.persist(prompt2);
        entityManager.persist(tag1);
        entityManager.persist(tag2);

        entityManager.flush();
    }

    @Test
    void 동일한_프롬프트와_태그_중복연결시_예외발생() {
        // given
        PromptTag promptTag1 = PromptTag.link(prompt1, tag1);
        promptTagRepository.save(promptTag1);
        entityManager.flush();

        // when & then
        PromptTag promptTag2 = PromptTag.link(prompt1, tag1);

        assertThatThrownBy(() -> {
            promptTagRepository.save(promptTag2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByPromptWithTag() {
        // given - 추가 태그 생성
        Tag tag3 = TagFixture.createTag("브랜딩");
        Tag tag4 = TagFixture.createTag("세일즈");
        entityManager.persist(tag3);
        entityManager.persist(tag4);

        // prompt1에는 tag1, tag2 연결
        PromptTag promptTag1 = PromptTag.link(prompt1, tag1); // prompt1 + 마케팅
        PromptTag promptTag2 = PromptTag.link(prompt1, tag2); // prompt1 + 광고

        // prompt2에는 tag3, tag4 연결 (완전히 다른 태그들)
        PromptTag promptTag3 = PromptTag.link(prompt2, tag3); // prompt2 + 브랜딩
        PromptTag promptTag4 = PromptTag.link(prompt2, tag4); // prompt2 + 세일즈

        promptTagRepository.saveAll(List.of(promptTag1, promptTag2, promptTag3, promptTag4));
        entityManager.flush();
        entityManager.clear();

        // when - prompt1만 조회
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(prompt1);

        // then - prompt1과 연결된 tag1, tag2만 가져와야 함 (tag3, tag4는 제외)
        assertThat(promptTags).hasSize(2);
        assertThat(promptTags)
                .extracting(pt -> pt.getTag().getName())
                .containsExactlyInAnyOrder("마케팅", "광고");
    }

    @Test
    void findByTagWithPrompt() {
        // given - 추가 프롬프트 2개만 생성
        Prompt prompt3 = PromptFixture.createPrompt("프롬프트3", "내용3", "설명3");
        Prompt prompt4 = PromptFixture.createPrompt("프롬프트4", "내용4", "설명4");

        // 기존 member, category 재사용
        ReflectionTestUtils.setField(prompt3, "member", member1);
        ReflectionTestUtils.setField(prompt3, "category", category1);
        ReflectionTestUtils.setField(prompt4, "member", member2);
        ReflectionTestUtils.setField(prompt4, "category", category2);

        entityManager.persist(prompt3);
        entityManager.persist(prompt4);

        // tag1(마케팅)에는 prompt1, prompt2 연결
        PromptTag promptTag1 = PromptTag.link(prompt1, tag1);
        PromptTag promptTag2 = PromptTag.link(prompt2, tag1);

        // tag2(광고)에는 prompt3, prompt4 연결
        PromptTag promptTag3 = PromptTag.link(prompt3, tag2);
        PromptTag promptTag4 = PromptTag.link(prompt4, tag2);

        promptTagRepository.saveAll(List.of(promptTag1, promptTag2, promptTag3, promptTag4));
        entityManager.flush();
        entityManager.clear();

        // when - tag1(마케팅)만 조회
        List<PromptTag> promptTags = promptTagRepository.findByTagWithPrompt(tag1);

        // then - tag1과 연결된 prompt1, prompt2만 가져와야 함
        assertThat(promptTags).hasSize(2);
        assertThat(promptTags)
                .extracting(pt -> pt.getPrompt().getTitle())
                .containsExactlyInAnyOrder("프롬프트1", "프롬프트2");
    }

    @Test
    void existsByPromptAndTag() {
        // given
        PromptTag promptTag = PromptTag.link(prompt1, tag1);
        promptTagRepository.save(promptTag);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThat(promptTagRepository.existsByPromptAndTag(prompt1, tag1)).isTrue();
        assertThat(promptTagRepository.existsByPromptAndTag(prompt1, tag2)).isFalse();
        assertThat(promptTagRepository.existsByPromptAndTag(prompt2, tag1)).isFalse();
        assertThat(promptTagRepository.existsByPromptAndTag(prompt2, tag2)).isFalse();
    }

    @Test
    void deleteByPrompt() {
        // given
        PromptTag promptTag1 = PromptTag.link(prompt1, tag1);
        PromptTag promptTag2 = PromptTag.link(prompt1, tag2);
        PromptTag promptTag3 = PromptTag.link(prompt2, tag1); // 다른 프롬프트와 연결
        promptTagRepository.saveAll(List.of(promptTag1, promptTag2, promptTag3));
        entityManager.flush();
        entityManager.clear();

        // when
        promptTagRepository.deleteByPrompt(prompt1);
        entityManager.flush();
        entityManager.clear();

        // then
        // prompt1과 연결된 PromptTag들은 삭제됨
        List<PromptTag> remainingPromptTags = promptTagRepository.findByPromptWithTag(prompt1);
        assertThat(remainingPromptTags).isEmpty();

        // prompt2와 연결된 PromptTag는 남아있음
        List<PromptTag> prompt2Tags = promptTagRepository.findByPromptWithTag(prompt2);
        assertThat(prompt2Tags).hasSize(1);

        // Tag 엔티티들은 삭제되지 않고 그대로 남아있음
        assertThat(entityManager.find(Tag.class, tag1.getId())).isNotNull();
        assertThat(entityManager.find(Tag.class, tag2.getId())).isNotNull();
    }
}