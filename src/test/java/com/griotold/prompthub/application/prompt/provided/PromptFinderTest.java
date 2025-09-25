package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.prompt.response.PromptListResponse;
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
    void findPopularPrompts_인기순_정렬된다() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt1 = createAndSavePrompt("프롬프트1", "내용", member, category);
        Prompt prompt2 = createAndSavePrompt("프롬프트2", "내용", member, category);
        Prompt prompt3 = createAndSavePrompt("프롬프트3", "내용", member, category);

        // 좋아요 개수 다르게 설정 - 1번 프롬프트 좋아요 3개 // 2번 프롬프트 좋아요 0개 // 3번 프롬프트 좋아요 1개
        prompt1.increaseLikeCount();
        prompt1.increaseLikeCount();
        prompt1.increaseLikeCount();
        prompt3.increaseLikeCount();
        promptRepository.save(prompt1);
        promptRepository.save(prompt3);

        entityManager.flush();
        entityManager.clear();

        // when
        Page<PromptListResponse> result = promptFinder.findPopularPrompts(PageRequest.of(0, 10));

        // then : 1 - 3 - 2 순
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).id()).isEqualTo(prompt1.getId());
        assertThat(result.getContent().get(1).id()).isEqualTo(prompt3.getId());
        assertThat(result.getContent().get(2).id()).isEqualTo(prompt2.getId());
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

    @Test
    void findPublicPrompts_전체_조회() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        createAndSavePrompt("공개 프롬프트1", "내용1", member, category);
        createAndSavePrompt("공개 프롬프트2", "내용2", member, category);

        Prompt privatePrompt = createAndSavePrompt("비공개 프롬프트", "비공개 내용", member, category);
        privatePrompt.makePrivate();
        promptRepository.save(privatePrompt);

        // when - categoryId, keyword 모두 null
        Page<PromptListResponse> responses = promptFinder.findPublicPrompts(null, null, PageRequest.of(0, 10));

        // then
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent())
                .extracting(PromptListResponse::title)
                .containsExactlyInAnyOrder("공개 프롬프트1", "공개 프롬프트2");

        // DTO 필드 검증
        PromptListResponse response = responses.getContent().get(0);
        assertThat(response.id()).isNotNull();
        assertThat(response.categoryName()).isEqualTo("콘텐츠 작성");
        assertThat(response.authorNickname()).isEqualTo("testnick");
        assertThat(response.viewsCount()).isEqualTo(0);
        assertThat(response.likesCount()).isEqualTo(0);
        assertThat(response.averageRating()).isEqualTo(0.0);
        assertThat(response.reviewsCount()).isEqualTo(0);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void findPublicPrompts_카테고리별_조회() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category1 = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Category category2 = createAndSaveCategory("업무 자동화", "업무용 프롬프트");

        createAndSavePrompt("카테고리1 프롬프트1", "내용1", member, category1);
        createAndSavePrompt("카테고리1 프롬프트2", "내용2", member, category1);
        createAndSavePrompt("카테고리2 프롬프트", "내용3", member, category2);

        // when - categoryId 지정
        Page<PromptListResponse> responses = promptFinder.findPublicPrompts(category1.getId(), null, PageRequest.of(0, 10));

        // then
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent())
                .extracting(PromptListResponse::title)
                .containsExactlyInAnyOrder("카테고리1 프롬프트1", "카테고리1 프롬프트2");
        assertThat(responses.getContent())
                .extracting(PromptListResponse::categoryName)
                .allMatch(name -> name.equals("콘텐츠 작성"));
    }

    @Test
    void findPublicPrompts_키워드_검색() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        createAndSavePrompt("블로그 작성 프롬프트", "블로그 내용", member, category);
        createAndSavePrompt("다른 제목", "블로그 관련 설명", member, category);
        createAndSavePrompt("관련없는 프롬프트", "관련없는 내용", member, category);

        // when - keyword 지정
        Page<PromptListResponse> responses = promptFinder.findPublicPrompts(null, "블로그", PageRequest.of(0, 10));

        // then
        assertThat(responses.getContent()).hasSize(2);
        assertThat(responses.getContent())
                .extracting(PromptListResponse::title)
                .containsExactlyInAnyOrder("블로그 작성 프롬프트", "다른 제목");
    }

    @Test
    void findPublicPrompts_빈_키워드() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        createAndSavePrompt("프롬프트1", "내용1", member, category);
        createAndSavePrompt("프롬프트2", "내용2", member, category);

        // when - 빈 키워드 (전체 조회와 동일하게 동작)
        Page<PromptListResponse> responses1 = promptFinder.findPublicPrompts(null, "", PageRequest.of(0, 10));
        Page<PromptListResponse> responses2 = promptFinder.findPublicPrompts(null, "  ", PageRequest.of(0, 10));

        // then
        assertThat(responses1.getContent()).hasSize(2);
        assertThat(responses2.getContent()).hasSize(2);
    }

    @Test
    void findPublicPrompts_존재하지_않는_카테고리() {
        // given
        Long nonExistentCategoryId = 999L;

        // when & then
        assertThatThrownBy(() -> promptFinder.findPublicPrompts(nonExistentCategoryId, null, PageRequest.of(0, 10)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("카테고리를 찾을 수 없습니다. id: 999");
    }

    @Test
    void findPublicPrompts_리뷰_정보_포함() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트 프롬프트", "내용", member, category);

        // 평점 추가 (Review 없이 직접 Rating 조작)
        prompt.addRating(5);
        prompt.addRating(4);
        promptRepository.save(prompt);
        entityManager.flush();
        entityManager.clear();

        // when
        Page<PromptListResponse> responses = promptFinder.findPublicPrompts(null, null, PageRequest.of(0, 10));

        // then
        PromptListResponse response = responses.getContent().get(0);
        assertThat(response.averageRating()).isEqualTo(4.5); // (5+4)/2
        assertThat(response.reviewsCount()).isEqualTo(2);
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