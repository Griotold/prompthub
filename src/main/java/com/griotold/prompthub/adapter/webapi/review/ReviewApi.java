package com.griotold.prompthub.adapter.webapi.review;

import com.griotold.prompthub.adapter.security.user.LoginUser;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.review.provided.ReviewFinder;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewApi {

    private final ReviewFinder reviewFinder;
    private final PromptFinder promptFinder;

    /**
     * 프롬프트별 리뷰 목록 조회 (내 리뷰 우선 + 최신순)
     */
    @GetMapping("/prompts/{promptId}/reviews")
    public ResponseEntity<BaseResponse<ReviewListResponse>> getPromptReviews(
            @PathVariable("promptId") Long promptId,
            @PageableDefault(size = 4, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 리뷰 목록 조회. promptId: {}, userId: {}", promptId, loginUser.getMember().getId());

        Prompt prompt = promptFinder.find(promptId);
        Member currentMember = loginUser.getMember();

        Slice<Review> reviewSlice = reviewFinder.findByPromptWithMyReviewFirst(prompt, currentMember, pageable);

        return BaseResponse.success(ReviewListResponse.from(reviewSlice));
    }
}