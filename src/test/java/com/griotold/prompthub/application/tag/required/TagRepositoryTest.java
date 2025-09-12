package com.griotold.prompthub.application.tag.required;

import com.griotold.prompthub.domain.tag.Tag;
import com.griotold.prompthub.domain.tag.TagFixture;
import com.griotold.prompthub.support.annotation.RepositoryTest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RepositoryTest
@RequiredArgsConstructor
class TagRepositoryTest {
    
    final TagRepository tagRepository;
    final EntityManager em;

    @Test
    void findByName() {
        // given
        Tag tag = TagFixture.createTag("마케팅");
        tagRepository.save(tag);
        em.flush();
        em.clear();

        // when
        Optional<Tag> foundTag = tagRepository.findByName("마케팅");
        Optional<Tag> notFoundTag = tagRepository.findByName("존재하지않는태그");

        // then
        assertThat(foundTag).isPresent();
        assertThat(foundTag.get().getName()).isEqualTo("마케팅");

        assertThat(notFoundTag).isEmpty();
    }

    @Test
    void findByNameIn() {
        // given
        Tag tag1 = TagFixture.createTag("마케팅");
        Tag tag2 = TagFixture.createTag("광고");
        Tag tag3 = TagFixture.createTag("브랜딩");
        tagRepository.saveAll(List.of(tag1, tag2, tag3));
        em.flush();
        em.clear();

        // when
        List<String> searchNames = List.of("마케팅", "광고", "존재하지않는태그");
        List<Tag> foundTags = tagRepository.findByNameIn(searchNames);

        // then
        assertThat(foundTags).hasSize(2);
        assertThat(foundTags)
                .extracting(Tag::getName)
                .containsExactlyInAnyOrder("마케팅", "광고");
    }

    @Test
    void findByNameIn_못찾으면_빈_리스트() {
        // when
        List<String> searchNames = List.of("광고", "브랜딩");
        List<Tag> foundTags = tagRepository.findByNameIn(searchNames);

        // then
        assertThat(foundTags).isEmpty();
    }
}