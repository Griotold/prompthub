package com.griotold.prompthub.application.prompt.provided;

import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.prompt.required.PromptTagRepository;
import com.griotold.prompthub.application.prompt.response.PromptDetailResponse;
import com.griotold.prompthub.application.tag.response.TagResponse;
import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.prompt.*;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewFixture;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ApplicationTest
record PromptRegisterTest(PromptRegister promptRegister,
                          PromptRepository promptRepository,
                          PromptLikeRepository promptLikeRepository,
                          PromptTagRepository promptTagRepository,
                          TagRepository tagRepository,
                          EntityManager entityManager) {

    @Test
    void register() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        // when
        PromptDetailResponse response = promptRegister.register(
                PromptFixture.createPromptRegisterRequest("새 프롬프트", "새 내용", "새 설명"),
                member, category
        );

        // then
        // PromptDetailResponse 검증
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("새 프롬프트");
        assertThat(response.content()).isEqualTo("새 내용");
        assertThat(response.description()).isEqualTo("새 설명");
        assertThat(response.isPublic()).isTrue();
        assertThat(response.isLiked()).isFalse();
        assertThat(response.likesCount()).isEqualTo(0);
        assertThat(response.viewsCount()).isEqualTo(0);
        assertThat(response.tags()).isEmpty(); // 태그 없음
        assertThat(response.authorNickname()).isEqualTo("testnick");
        assertThat(response.category().name()).isEqualTo("콘텐츠 작성");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        // DB에도 제대로 저장되었는지 추가 검증
        Prompt savedPrompt = promptRepository.findById(response.id()).get();
        assertThat(savedPrompt.getTitle()).isEqualTo("새 프롬프트");
        assertThat(savedPrompt.getContent()).isEqualTo("새 내용");
    }

    @Test
    void register_태그와_함께() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        List<String> tags = List.of("자바", "스프링", "JPA");
        PromptRegisterRequest request = PromptFixture.createPromptRegisterRequest(
                "새 프롬프트", "새 내용", "새 설명", tags
        );

        // when
        PromptDetailResponse response = promptRegister.register(request, member, category);

        // then
        // 1. Response에 태그 정보가 포함되어 있는지 검증
        assertThat(response.tags()).hasSize(3);
        assertThat(response.tags())
                .extracting(TagResponse::name)
                .containsExactlyInAnyOrder("자바", "스프링", "JPA");

        // 2. DB에 태그들이 실제로 저장되었는지 검증
        List<Tag> savedTags = tagRepository.findByNameIn(tags);
        assertThat(savedTags).hasSize(3);
        assertThat(savedTags)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("자바", "스프링", "JPA");

        // 3. 프롬프트-태그 연결이 실제로 DB에 저장되었는지 검증
        Prompt savedPrompt = promptRepository.findById(response.id()).get();
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(savedPrompt);

        assertThat(promptTags).hasSize(3);
        assertThat(promptTags)
                .extracting(promptTag -> promptTag.getTag().getName())
                .containsExactlyInAnyOrder("자바", "스프링", "JPA");

        // 4. 각 PromptTag의 연결이 올바른지 검증
        promptTags.forEach(promptTag -> {
            assertThat(promptTag.getPrompt().getId()).isEqualTo(response.id());
            assertThat(promptTag.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void register_빈_태그_리스트() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");

        PromptRegisterRequest request = PromptFixture.createPromptRegisterRequest(
                "새 프롬프트", "새 내용", "새 설명", List.of()
        );

        // when
        PromptDetailResponse response = promptRegister.register(request, member, category);

        // then
        assertThat(response.tags()).isEmpty();

        // DB에도 태그 연결이 없는지 확인
        Prompt savedPrompt = promptRepository.findById(response.id()).get();
        List<PromptTag> promptTags = promptTagRepository.findByPromptWithTag(savedPrompt);
        assertThat(promptTags).isEmpty();
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

    @Test
    void addReview() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        // 리뷰 생성 (임시로 Review.register 사용)
        Review review = Review.register(
                ReviewFixture.createReviewRegisterRequest(5, "훌륭한 프롬프트입니다"),
                prompt, member
        );

        // when
        promptRegister.addReview(prompt, review);

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getReviewsCount()).isEqualTo(1);
        assertThat(updated.getAverageRating()).isEqualTo(5.0);
        assertThat(updated.hasReviews()).isTrue();
    }

    @Test
    void addReview_여러개() {
        // given
        Member member1 = createAndSaveMember("test1@test.com", "testnick1");
        Member member2 = createAndSaveMember("test2@test.com", "testnick2");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member1, category);

        Review review1 = Review.register(
                ReviewFixture.createReviewRegisterRequest(5, "훌륭합니다"),
                prompt, member1
        );
        Review review2 = Review.register(
                ReviewFixture.createReviewRegisterRequest(3, "괜찮습니다"),
                prompt, member2
        );

        // when
        promptRegister.addReview(prompt, review1);
        promptRegister.addReview(prompt, review2);

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getReviewsCount()).isEqualTo(2);
        assertThat(updated.getAverageRating()).isEqualTo(4.0); // (5+3)/2
    }

    @Test
    void updateReview() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        Review oldReview = Review.register(
                ReviewFixture.createReviewRegisterRequest(3, "보통입니다"),
                prompt, member
        );
        promptRegister.addReview(prompt, oldReview);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인
        Prompt beforeUpdate = promptRepository.findById(prompt.getId()).get();
        assertThat(beforeUpdate.getReviewsCount()).isEqualTo(1);
        assertThat(beforeUpdate.getAverageRating()).isEqualTo(3.0);

        Review newReview = Review.register(
                ReviewFixture.createReviewRegisterRequest(5, "수정: 훌륭합니다"),
                prompt, member
        );

        // when
        promptRegister.updateReview(beforeUpdate, oldReview.getRating(), newReview.getRating());
        entityManager.flush();
        entityManager.clear();

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getReviewsCount()).isEqualTo(1); // 개수는 변화없음
        assertThat(updated.getAverageRating()).isEqualTo(5.0); // 평점만 변경
    }

    @Test
    void removeReview() {
        // given
        Member member1 = createAndSaveMember("test1@test.com", "testnick1");
        Member member2 = createAndSaveMember("test2@test.com", "testnick2");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member1, category);

        Review review1 = Review.register(
                ReviewFixture.createReviewRegisterRequest(5, "훌륭합니다"),
                prompt, member1
        );
        Review review2 = Review.register(
                ReviewFixture.createReviewRegisterRequest(3, "괜찮습니다"),
                prompt, member2
        );

        promptRegister.addReview(prompt, review1);
        promptRegister.addReview(prompt, review2);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인
        Prompt beforeRemove = promptRepository.findById(prompt.getId()).get();
        assertThat(beforeRemove.getReviewsCount()).isEqualTo(2);
        assertThat(beforeRemove.getAverageRating()).isEqualTo(4.0); // (5+3)/2

        // when
        promptRegister.removeReview(beforeRemove, review1);
        entityManager.flush();
        entityManager.clear();

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getReviewsCount()).isEqualTo(1);
        assertThat(updated.getAverageRating()).isEqualTo(3.0); // review2만 남음
    }

    @Test
    void removeReview_마지막_리뷰() {
        // given
        Member member = createAndSaveMember("test@test.com", "testnick");
        Category category = createAndSaveCategory("콘텐츠 작성", "블로그용 프롬프트");
        Prompt prompt = createAndSavePrompt("테스트", "내용", member, category);

        Review review = Review.register(
                ReviewFixture.createReviewRegisterRequest(4, "좋습니다"),
                prompt, member
        );
        promptRegister.addReview(prompt, review);
        entityManager.flush();
        entityManager.clear();

        // 기존 상태 확인
        Prompt beforeRemove = promptRepository.findById(prompt.getId()).get();
        assertThat(beforeRemove.getReviewsCount()).isEqualTo(1);
        assertThat(beforeRemove.getAverageRating()).isEqualTo(4.0);
        assertThat(beforeRemove.hasReviews()).isTrue();

        // when
        promptRegister.removeReview(beforeRemove, review);
        entityManager.flush();
        entityManager.clear();

        // then
        Prompt updated = promptRepository.findById(prompt.getId()).get();
        assertThat(updated.getReviewsCount()).isEqualTo(0);
        assertThat(updated.getAverageRating()).isEqualTo(0.0);
        assertThat(updated.hasReviews()).isFalse();
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