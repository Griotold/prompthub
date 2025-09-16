package com.griotold.prompthub.application.tag.provided;

import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagFixture;
import com.griotold.prompthub.domain.tag.TagNames;
import com.griotold.prompthub.domain.tag.Tags;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationTest
record TagRegisterTest(TagRegister tagRegister,
                       TagRepository tagRepository,
                       EntityManager entityManager) {

    @Test
    void register() {
        Tag tag = TagFixture.createTag("Spring");

        Tag saved = tagRegister.register(tag);
        entityManager.flush();
        entityManager.clear();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Spring");
        assertThat(saved.getCreatedAt()).isNotNull();

        // DB에 실제로 저장되었는지 확인
        Tag found = tagRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("Spring");
    }

    @Test
    void ensureTags_모두_새로운_태그() {
        TagNames tagNames = TagNames.of(List.of("Spring", "JPA", "MySQL"));

        Tags result = tagRegister.ensureTags(tagNames);

        assertThat(result.toList()).hasSize(3);
        assertThat(result.toList()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL");

        // DB에 실제로 저장되었는지 확인
        assertThat(tagRepository.findByNameIn(tagNames.toList())).hasSize(3);
    }

    @Test
    void ensureTags_일부_기존_태그() {
        // Given: 기존 태그 저장
        tagRepository.save(TagFixture.createTag("Spring"));
        tagRepository.save(TagFixture.createTag("JPA"));
        entityManager.flush();
        entityManager.clear();

        TagNames tagNames = TagNames.of(List.of("Spring", "JPA", "MySQL", "Redis"));

        Tags result = tagRegister.ensureTags(tagNames);

        assertThat(result.toList()).hasSize(4);
        assertThat(result.toList()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL", "Redis");

        // 총 4개 태그가 DB에 있어야 함
        assertThat(tagRepository.findByNameIn(tagNames.toList())).hasSize(4);
    }

    @Test
    void ensureTags_모두_기존_태그() {
        // Given: 기존 태그들 저장
        tagRepository.save(TagFixture.createTag("Spring"));
        tagRepository.save(TagFixture.createTag("JPA"));
        entityManager.flush();
        entityManager.clear();

        TagNames tagNames = TagNames.of(List.of("Spring", "JPA"));

        Tags result = tagRegister.ensureTags(tagNames);

        assertThat(result.toList()).hasSize(2);
        assertThat(result.toList()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "JPA");

        // 새로 생성되지 않았는지 확인 (여전히 2개)
        assertThat(tagRepository.count()).isEqualTo(2);
    }

    @Test
    void ensureTags_빈_리스트() {
        TagNames tagNames = TagNames.of(List.of());

        Tags result = tagRegister.ensureTags(tagNames);

        assertThat(result.toList()).isEmpty();
        assertThat(tagRepository.count()).isEqualTo(0);
    }

    @Test
    void ensureTags_null_입력() {
        Tags result = tagRegister.ensureTags(null);

        assertThat(result.toList()).isEmpty();
        assertThat(tagRepository.count()).isEqualTo(0);
    }

    @Test
    void ensureTags_중복_태그명_입력() {
        TagNames tagNames = TagNames.of(List.of("Spring", "JPA", "Spring", "MySQL", "JPA"));

        Tags result = tagRegister.ensureTags(tagNames);

        // 중복 제거되어 3개만 생성되어야 함
        assertThat(result.toList()).hasSize(3);
        assertThat(result.toList()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "JPA", "MySQL");

        assertThat(tagRepository.count()).isEqualTo(3);
    }

    @Test
    void ensureTags_순서_보장() {
        TagNames tagNames = TagNames.of(List.of("Spring", "JPA", "MySQL"));

        Tags result = tagRegister.ensureTags(tagNames);

        // 입력 순서와 동일하게 반환되는지 확인 (기존 + 새로운 순서)
        assertThat(result.toList()).extracting(Tag::getName)
                .containsExactly("Spring", "JPA", "MySQL");
    }
}