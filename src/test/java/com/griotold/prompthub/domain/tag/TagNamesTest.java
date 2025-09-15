package com.griotold.prompthub.domain.tag;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TagNamesTest {

    @Test
    void of_정상생성() {
        List<String> names = List.of("Spring", "JPA", "MySQL");

        TagNames tagNames = TagNames.of(names);

        assertThat(tagNames.toList()).hasSize(3);
        assertThat(tagNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void of_빈리스트() {
        TagNames tagNames = TagNames.of(List.of());

        assertThat(tagNames.toList()).isEmpty();
    }

    @Test
    void of_null입력시_예외발생() {
        assertThatThrownBy(() -> TagNames.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void distinct_중복제거() {
        List<String> names = List.of("Spring", "JPA", "Spring", "MySQL", "JPA");
        TagNames tagNames = TagNames.of(names);

        TagNames distinctNames = tagNames.distinct();

        assertThat(distinctNames.toList()).hasSize(3);
        assertThat(distinctNames.toList()).containsExactlyInAnyOrder("Spring", "JPA", "MySQL");
    }

    @Test
    void distinct_중복없으면_그대로() {
        List<String> names = List.of("Spring", "JPA", "MySQL");
        TagNames tagNames = TagNames.of(names);

        TagNames distinctNames = tagNames.distinct();

        assertThat(distinctNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void 불변성_검증_원본리스트_변경해도_영향없음() {
        List<String> originalList = List.of("Spring", "JPA");
        TagNames tagNames = TagNames.of(originalList);

        // 반환된 리스트 변경 시도 (불변 리스트라 예외 발생해야 함)
        assertThatThrownBy(() -> tagNames.toList().add("MySQL"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 불변성_검증_distinct_연산후_원본_변경없음() {
        List<String> names = List.of("Spring", "JPA", "Spring");
        TagNames original = TagNames.of(names);

        TagNames distinct = original.distinct();

        // 원본은 변경되지 않아야 함
        assertThat(original.toList()).hasSize(3);
        assertThat(original.toList()).containsExactly("Spring", "JPA", "Spring");

        // 새 객체는 중복 제거되어야 함
        assertThat(distinct.toList()).hasSize(2);
        assertThat(distinct.toList()).containsExactlyInAnyOrder("Spring", "JPA");
    }

    @Test
    void 비즈니스_요구사항_검증_프롬프트등록시_태그중복제거() {
        // Given: 사용자가 프롬프트 등록 시 중복된 태그 입력
        List<String> userInputTags = List.of("Spring", "Backend", "Spring", "JPA", "Backend");

        // When: 중복 제거 처리
        TagNames tagNames = TagNames.of(userInputTags).distinct();

        // Then: 중복이 제거되어야 함
        assertThat(tagNames.toList()).hasSize(3);
        assertThat(tagNames.toList()).containsExactlyInAnyOrder("Spring", "Backend", "JPA");
    }

    @Test
    void filterExisting_기존태그_제외() {
        TagNames allTagNames = TagNames.of(List.of("Spring", "JPA", "MySQL", "Redis"));
        TagNames existingTagNames = TagNames.of(List.of("Spring", "JPA"));

        TagNames newTagNames = allTagNames.filterExisting(existingTagNames);

        assertThat(newTagNames.toList()).hasSize(2);
        assertThat(newTagNames.toList()).containsExactlyInAnyOrder("MySQL", "Redis");
    }

    @Test
    void filterExisting_모두_기존태그() {
        TagNames allTagNames = TagNames.of(List.of("Spring", "JPA"));
        TagNames existingTagNames = TagNames.of(List.of("Spring", "JPA", "MySQL"));

        TagNames newTagNames = allTagNames.filterExisting(existingTagNames);

        assertThat(newTagNames.toList()).isEmpty();
    }

    @Test
    void filterExisting_기존태그_없음() {
        TagNames allTagNames = TagNames.of(List.of("Spring", "JPA", "MySQL"));
        TagNames existingTagNames = TagNames.of(List.of("Redis", "Docker"));

        TagNames newTagNames = allTagNames.filterExisting(existingTagNames);

        assertThat(newTagNames.toList()).hasSize(3);
        assertThat(newTagNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void filterExisting_빈_기존태그() {
        TagNames allTagNames = TagNames.of(List.of("Spring", "JPA"));
        TagNames existingTagNames = TagNames.of(List.of());

        TagNames newTagNames = allTagNames.filterExisting(existingTagNames);

        assertThat(newTagNames.toList()).containsExactly("Spring", "JPA");
    }

    @Test
    void 비즈니스_요구사항_검증_새로생성할_태그명_필터링() {
        // Given: 사용자 입력 태그명들과 이미 존재하는 태그명들
        TagNames userInputTags = TagNames.of(List.of("Spring", "JPA", "MySQL", "Redis"));
        TagNames existingTags = TagNames.of(List.of("Spring", "JPA"));

        // When: 새로 생성해야 할 태그명들 필터링
        TagNames tagsToCreate = userInputTags.filterExisting(existingTags);

        // Then: 존재하지 않는 태그명들만 남아야 함
        assertThat(tagsToCreate.toList()).containsExactlyInAnyOrder("MySQL", "Redis");
    }

    @Test
    void ofDistinct_중복제거하여_생성() {
        List<String> names = List.of("Spring", "JPA", "Spring", "MySQL", "JPA");

        TagNames tagNames = TagNames.ofDistinct(names);

        assertThat(tagNames.toList()).hasSize(3);
        assertThat(tagNames.toList()).containsExactlyInAnyOrder("Spring", "JPA", "MySQL");
    }

    @Test
    void ofDistinct_중복없으면_그대로_생성() {
        List<String> names = List.of("Spring", "JPA", "MySQL");

        TagNames tagNames = TagNames.ofDistinct(names);

        assertThat(tagNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void ofDistinct_빈리스트() {
        TagNames tagNames = TagNames.ofDistinct(List.of());

        assertThat(tagNames.toList()).isEmpty();
    }

    @Test
    void ofDistinct_null입력시_예외발생() {
        assertThatThrownBy(() -> TagNames.ofDistinct(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 비즈니스_요구사항_검증_프롬프트등록시_중복제거된_태그명_생성() {
        // Given: 사용자가 프롬프트 등록 시 중복된 태그 입력
        List<String> userInputTags = List.of("Spring", "Backend", "Spring", "JPA", "Backend", "API");

        // When: ofDistinct로 중복 제거하며 생성
        TagNames distinctTagNames = TagNames.ofDistinct(userInputTags);

        // Then: 중복이 제거된 태그명들이 생성되어야 함
        assertThat(distinctTagNames.toList()).hasSize(4);
        assertThat(distinctTagNames.toList()).containsExactlyInAnyOrder("Spring", "Backend", "JPA", "API");
    }

    @Test
    void ofDistinct와_of_distinct_동일한_결과() {
        List<String> names = List.of("Spring", "JPA", "Spring", "MySQL");

        TagNames fromOfDistinct = TagNames.ofDistinct(names);
        TagNames fromOfThenDistinct = TagNames.of(names).distinct();

        assertThat(fromOfDistinct.toList()).isEqualTo(fromOfThenDistinct.toList());
    }

    @Test
    void createTags_태그명들로_Tag객체_생성() {
        TagNames tagNames = TagNames.of(List.of("Spring", "JPA", "MySQL"));

        List<Tag> tags = tagNames.createTags();

        assertThat(tags).hasSize(3);
        assertThat(tags).extracting(Tag::getName)
                .containsExactly("Spring", "JPA", "MySQL");
        assertThat(tags).allMatch(tag -> tag.getId() == null); // 아직 저장되지 않은 상태
        assertThat(tags).allMatch(tag -> tag.getCreatedAt() == null); // 아직 저장되지 않은 상태
    }

    @Test
    void createTags_빈TagNames() {
        TagNames emptyTagNames = TagNames.of(List.of());

        List<Tag> tags = emptyTagNames.createTags();

        assertThat(tags).isEmpty();
    }

    @Test
    void createTags_단일_태그명() {
        TagNames singleTagName = TagNames.of(List.of("Spring"));

        List<Tag> tags = singleTagName.createTags();

        assertThat(tags).hasSize(1);
        assertThat(tags.get(0).getName()).isEqualTo("Spring");
    }

    @Test
    void 비즈니스_요구사항_검증_새로운_태그들_생성() {
        // Given: 새로 생성해야 할 태그명들
        TagNames newTagNames = TagNames.of(List.of("MySQL", "Redis", "Docker"));

        // When: Tag 객체들 생성
        List<Tag> newTags = newTagNames.createTags();

        // Then: 각 태그명에 대응하는 Tag 객체들이 생성되어야 함
        assertThat(newTags).hasSize(3);
        assertThat(newTags).extracting(Tag::getName)
                .containsExactly("MySQL", "Redis", "Docker");

        // 모든 태그가 새로 생성된 상태여야 함 (ID가 없음)
        assertThat(newTags).allMatch(tag -> tag.getId() == null);
    }

    @Test
    void createTags_불변성_검증() {
        TagNames tagNames = TagNames.of(List.of("Spring", "JPA"));

        List<Tag> tags1 = tagNames.createTags();
        List<Tag> tags2 = tagNames.createTags();

        // 매번 새로운 Tag 객체들이 생성되어야 함
        assertThat(tags1).isNotSameAs(tags2);
        assertThat(tags1.get(0)).isNotSameAs(tags2.get(0));

        // 하지만 내용은 동일해야 함
        assertThat(tags1).extracting(Tag::getName)
                .isEqualTo(tags2.stream().map(Tag::getName).toList());
    }
}