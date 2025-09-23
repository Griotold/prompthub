package com.griotold.prompthub.application.review;

import com.griotold.prompthub.application.review.provided.ReviewFinder;
import com.griotold.prompthub.application.review.required.ReviewRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Validated
@RequiredArgsConstructor
public class ReviewQueryService implements ReviewFinder {

    private final ReviewRepository reviewRepository;

    @Override
    public Review find(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. id: " + reviewId));
    }

    @Override
    public List<Review> findByPrompt(Prompt prompt) {
        return reviewRepository.findByPrompt(prompt);
    }

    @Override
    public List<Review> findByMember(Member member) {
        return reviewRepository.findByMember(member);
    }

    @Override
    public Review findByPromptAndMember(Prompt prompt, Member member) {
        return reviewRepository.findByPromptAndMember(prompt, member)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 프롬프트에 대한 리뷰를 찾을 수 없습니다. promptId: " + prompt.getId() + ", memberId: " + member.getId()));
    }

    @Override
    public Slice<Review> findByPromptExcludingMember(Prompt prompt, Member excludeMember, Pageable pageable) {
        return reviewRepository.findByPromptExcludingMember(prompt, excludeMember, pageable);
    }

    @Override
    public Slice<Review> findByPromptWithMyReviewFirst(Prompt prompt, Member member, Pageable pageable) {
        // 첫 번째 페이지가 아니면 내 리뷰 제외하고 조회
        if (pageable.getOffset() != 0) {
            return reviewRepository.findByPromptExcludingMember(prompt, member, pageable);
        }

        // 첫 번째 페이지에서 내 리뷰 찾기
        Optional<Review> myReview = reviewRepository.findByPromptAndMember(prompt, member);
        if (myReview.isEmpty()) {
            return reviewRepository.findByPromptExcludingMember(prompt, member, pageable);
        }

        // 내 리뷰가 있으면 첫 번째에 배치
        return buildSliceWithMyReviewFirst(myReview.get(), prompt, member, pageable);
    }

    private Slice<Review> buildSliceWithMyReviewFirst(Review myReview, Prompt prompt, Member member, Pageable pageable) {
        // 다른 사용자 리뷰 조회 (size 1 감소)
        Pageable adjustedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize() - 1,
                pageable.getSort()
        );

        Slice<Review> otherReviews = reviewRepository.findByPromptExcludingMember(prompt, member, adjustedPageable);

        // 내 리뷰 + 다른 리뷰들 결합
        List<Review> allReviews = new ArrayList<>();
        allReviews.add(myReview);
        allReviews.addAll(otherReviews.getContent());

        return new SliceImpl<>(allReviews, pageable, otherReviews.hasNext());
    }
}