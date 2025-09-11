package com.griotold.prompthub.application.tag.required;

import com.griotold.prompthub.domain.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 태그 이름으로 찾기 (태그 존재 여부 확인용)
    Optional<Tag> findByName(String name);

    // 여러 태그 이름으로 한번에 찾기
    List<Tag> findByNameIn(List<String> names);

    /**
     * 사용 빈도순 태그 조회 (인기 태그)
     *
     * PromptTag와 조인하여 많이 사용된 태그 순으로 정렬.
     * 메인 페이지의 인기 태그, 태그 자동완성, 추천 태그 등에 활용 예정.
     * MVP 완료 후 태그 관련 기능 확장 시 구현할 예정.
     *
     * @param pageable 페이징 정보
     * @return 사용 빈도 내림차순으로 정렬된 태그 목록
     */
    //@Query("SELECT t FROM Tag t JOIN PromptTag pt ON t.id = pt.tag.id GROUP BY t ORDER BY COUNT(pt) DESC")
    //List<Tag> findPopularTags(Pageable pageable);
}
