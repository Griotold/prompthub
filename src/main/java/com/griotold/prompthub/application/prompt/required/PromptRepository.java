package com.griotold.prompthub.application.prompt.required;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromptRepository extends JpaRepository<Prompt, Long> {

    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.id = :id")
    Optional<Prompt> findByIdWithCategoryAndMember(@Param("id") Long id);

    // 공개 프롬프트 조회 (메인 페이지)
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.isPublic = true ORDER BY p.createdAt DESC")
    Page<Prompt> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);

    // 카테고리별 조회
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.category = :category AND p.isPublic = true ORDER BY p.createdAt DESC")
    Page<Prompt> findByCategoryAndIsPublicTrueOrderByCreatedAtDesc(@Param("category") Category category, Pageable pageable);

    // 사용자별 프롬프트 (마이페이지)
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.member = :member ORDER BY p.createdAt DESC")
    Page<Prompt> findByMemberOrderByCreatedAtDesc(@Param("member") Member member, Pageable pageable);

    // 검색 (제목, 내용)
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.isPublic = true AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.createdAt DESC")
    Page<Prompt> findByIsPublicTrueAndTitleContainingOrContentContainingOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    // 인기 프롬프트 (좋아요순)
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member WHERE p.isPublic = true ORDER BY p.likesCount DESC, p.createdAt DESC")
    Page<Prompt> findByIsPublicTrueOrderByLikesCountDescCreatedAtDesc(Pageable pageable);

    // 좋아요한 프롬프트 목록
    @Query("SELECT p FROM Prompt p JOIN FETCH p.category JOIN FETCH p.member JOIN PromptLike pl ON p.id = pl.prompt.id WHERE pl.member = :member ORDER BY pl.createdAt DESC")
    Page<Prompt> findLikedByMember(@Param("member") Member member, Pageable pageable);
}
