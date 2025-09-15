package com.griotold.prompthub.domain.tag;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class TagsTest {

    @Test
    void of_정상생성() {
        List<Tag> tagList = List.of(
                TagFixture.createTag("Spring"),
                TagFixture.createTag("JPA"),
                TagFixture.createTag("MySQL")
        );

        Tags tags = Tags.of(tagList);

        assertThat(tags.toList()).hasSize(3);
        assertThat(tags.toList()).extracting(Tag::getName)
                .containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void of_빈리스트() {
        Tags tags = Tags.of(List.of());

        assertThat(tags.toList()).isEmpty();
        assertThat(tags.isEmpty()).isTrue();
    }

    @Test
    void of_null입력시_예외발생() {
        assertThatThrownBy(() -> Tags.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isEmpty_빈컬렉션일때_true() {
        Tags tags = Tags.of(List.of());

        assertThat(tags.isEmpty()).isTrue();
    }

    @Test
    void isEmpty_요소있을때_false() {
        Tags tags = Tags.of(List.of(TagFixture.createTag("Spring")));

        assertThat(tags.isEmpty()).isFalse();
    }

    @Test
    void extractNames_태그명_추출() {
        List<Tag> tagList = List.of(
                TagFixture.createTag("Spring"),
                TagFixture.createTag("JPA"),
                TagFixture.createTag("MySQL")
        );
        Tags tags = Tags.of(tagList);

        TagNames tagNames = tags.extractNames();

        assertThat(tagNames.toList()).hasSize(3);
        assertThat(tagNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }

    @Test
    void combine_두_Tags_합치기() {
        Tags existingTags = Tags.of(List.of(
                TagFixture.createTag("Spring"),
                TagFixture.createTag("JPA")
        ));

        Tags newTags = Tags.of(List.of(
                TagFixture.createTag("MySQL"),
                TagFixture.createTag("Redis")
        ));

        Tags combined = existingTags.combine(newTags);

        assertThat(combined.toList()).hasSize(4);
        assertThat(combined.toList()).extracting(Tag::getName)
                .containsExactly("Spring", "JPA", "MySQL", "Redis");
    }

    @Test
    void combine_빈Tags와_합치기() {
        Tags existingTags = Tags.of(List.of(TagFixture.createTag("Spring")));
        Tags emptyTags = Tags.of(List.of());

        Tags combined = existingTags.combine(emptyTags);

        assertThat(combined.toList()).hasSize(1);
        assertThat(combined.toList()).extracting(Tag::getName)
                .containsExactly("Spring");
    }

    @Test
    void 불변성_검증_원본리스트_변경해도_영향없음() {
        Tags tags = Tags.of(List.of(TagFixture.createTag("Spring")));

        // 반환된 리스트 변경 시도 (불변 리스트라 예외 발생해야 함)
        assertThatThrownBy(() -> tags.toList().add(TagFixture.createTag("JPA")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void 불변성_검증_combine_연산후_원본_변경없음() {
        Tags original = Tags.of(List.of(TagFixture.createTag("Spring")));
        Tags additional = Tags.of(List.of(TagFixture.createTag("JPA")));

        Tags combined = original.combine(additional);

        // 원본들은 변경되지 않아야 함
        assertThat(original.toList()).hasSize(1);
        assertThat(additional.toList()).hasSize(1);

        // 새 객체만 합쳐진 결과를 가져야 함
        assertThat(combined.toList()).hasSize(2);
    }

    @Test
    void 비즈니스_요구사항_검증_기존태그와_새태그_합치기() {
        // Given: 기존에 있던 태그들
        Tags existingTags = Tags.of(List.of(
                TagFixture.createTag("Spring"),
                TagFixture.createTag("JPA")
        ));

        // 새로 생성된 태그들
        Tags newlyCreatedTags = Tags.of(List.of(
                TagFixture.createTag("MySQL"),
                TagFixture.createTag("Redis")
        ));

        // When: 기존 + 새로 생성된 태그들 모두 반환
        Tags allTags = existingTags.combine(newlyCreatedTags);

        // Then: 모든 태그가 포함되어야 함
        assertThat(allTags.toList()).hasSize(4);
        assertThat(allTags.extractNames().toList())
                .containsExactly("Spring", "JPA", "MySQL", "Redis");
    }

    @Test
    void 비즈니스_요구사항_검증_태그명_추출_후_기존태그_찾기() {
        // Given: 프롬프트에 연결된 태그들
        Tags promptTags = Tags.of(List.of(
                TagFixture.createTag("Spring"),
                TagFixture.createTag("JPA"),
                TagFixture.createTag("MySQL")
        ));

        // When: 태그명들을 추출 (기존 태그 존재 여부 확인용)
        TagNames extractedNames = promptTags.extractNames();

        // Then: 태그명들이 올바르게 추출되어야 함
        assertThat(extractedNames.toList()).containsExactly("Spring", "JPA", "MySQL");
    }
}