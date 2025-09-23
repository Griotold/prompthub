package com.griotold.prompthub.application.review.required;

import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // findById + Member fetch join
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.id = :reviewId")
    Optional<Review> findByIdWithMember(@Param("reviewId") Long reviewId);

    // 프롬프트의 모든 리뷰 조회
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.prompt = :prompt")
    List<Review> findByPrompt(Prompt prompt);

    // 사용자의 모든 리뷰 조회
    List<Review> findByMember(Member member);

    // 특정 프롬프트에 특정 사용자가 작성한 리뷰 (중복 방지) + Member fetch join
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.prompt = :prompt AND r.member = :member")
    Optional<Review> findByPromptAndMember(@Param("prompt") Prompt prompt, @Param("member") Member member);


    // 특정 사용자를 제외한 프롬프트의 리뷰들 조회 (인프런식 더보기) + Member fetch join 추가
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.prompt = :prompt AND r.member != :excludeMember ORDER BY r.createdAt DESC")
    Slice<Review> findByPromptExcludingMember(@Param("prompt") Prompt prompt, @Param("excludeMember") Member excludeMember, Pageable pageable);

    /**
     * 프롬프트별 리뷰 Slice 조회 (최신순) + Member fetch join 추가
     * todo
     * NOTE: MVP에서는 findByPromptExcludingMember를 사용하여
     * 로그인 여부와 상관없이 일관된 UI를 제공할 예정이므로
     * 현재 버전에서는 사용되지 않을 가능성이 높음.
     * 추후 관리자 기능이나 특별한 요구사항이 생기면 활용 예정.
     */
    @Query("SELECT r FROM Review r JOIN FETCH r.member WHERE r.prompt = :prompt ORDER BY r.createdAt DESC")
    Slice<Review> findByPromptOrderByCreatedAtDesc(@Param("prompt") Prompt prompt, Pageable pageable);
}
