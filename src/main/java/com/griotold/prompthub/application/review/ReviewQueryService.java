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
        // 첫 번째 페이지인 경우 (offset = 0)
        if (pageable.getOffset() == 0) {
            // 내 리뷰가 있는지 확인
            Optional<Review> myReview = reviewRepository.findByPromptAndMember(prompt, member);

            if (myReview.isPresent()) {
                // 내 리뷰가 있으면 size를 1 줄여서 다른 사람들 리뷰 조회
                Pageable adjustedPageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize() - 1,
                        pageable.getSort()
                );

                Slice<Review> otherReviews = reviewRepository.findByPromptExcludingMember(
                        prompt, member, adjustedPageable
                );

                // 내 리뷰를 맨 앞에 추가한 새로운 리스트 생성
                List<Review> combinedReviews = new ArrayList<>();
                combinedReviews.add(myReview.get());
                combinedReviews.addAll(otherReviews.getContent());

                return new SliceImpl<>(combinedReviews, pageable, otherReviews.hasNext());
            }
        }

        // 첫 페이지이지만 내 리뷰가 없거나, 두 번째 페이지부터는 다른 사람들 리뷰만
        return reviewRepository.findByPromptExcludingMember(prompt, member, pageable);
    }
}