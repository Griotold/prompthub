package com.griotold.prompthub.application.tag.provided;

import com.griotold.prompthub.application.tag.required.TagRepository;
import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagFixture;
import com.griotold.prompthub.support.annotation.ApplicationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@ApplicationTest
record TagFinderTest(TagFinder tagFinder,
                    TagRepository tagRepository,
                    EntityManager entityManager) {

    @Test
    void find() {
        Tag tag = tagRepository.save(TagFixture.createTag("Spring"));
        entityManager.flush();
        entityManager.clear();

        Tag found = tagFinder.find(tag.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getName()).isEqualTo("Spring");
    }

    @Test
    void findById_없는_id일때() {
        assertThatThrownBy(() -> tagFinder.find(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("태그를 찾을 수 없습니다. id: 999");
    }

    @Test
    void findByName() {
        Tag tag = tagRepository.save(TagFixture.createTag("JPA"));
        entityManager.flush();
        entityManager.clear();

        Tag found = tagFinder.findByName("JPA");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getName()).isEqualTo("JPA");
    }

    @Test
    void findByName_없는_name일때() {
        assertThatThrownBy(() -> tagFinder.findByName("없는태그"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("태그를 찾을 수 없습니다. name: 없는태그");
    }

    @Test
    void findByNames() {
        Tag tag1 = tagRepository.save(TagFixture.createTag("Spring"));
        Tag tag2 = tagRepository.save(TagFixture.createTag("JPA"));
        Tag tag3 = tagRepository.save(TagFixture.createTag("MySQL"));
        entityManager.flush();
        entityManager.clear();

        List<String> tagNames = List.of("Spring", "JPA", "없는태그");
        List<Tag> found = tagFinder.findByNames(tagNames);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Spring", "JPA");
    }

    @Test
    void findByNames_모두_존재하지_않을때() {
        List<String> tagNames = List.of("없는태그1", "없는태그2");
        List<Tag> found = tagFinder.findByNames(tagNames);

        assertThat(found).isEmpty();
    }

    @Test
    void findByNames_빈_리스트일때() {
        List<Tag> found = tagFinder.findByNames(List.of());

        assertThat(found).isEmpty();
    }
}