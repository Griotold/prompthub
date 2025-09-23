package com.griotold.prompthub.application.review;

import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.prompt.provided.PromptRegister;
import com.griotold.prompthub.application.review.provided.ReviewFinder;
import com.griotold.prompthub.application.review.provided.ReviewRegister;
import com.griotold.prompthub.application.review.required.ReviewRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.review.ReviewChange;
import com.griotold.prompthub.domain.review.ReviewRegisterRequest;
import com.griotold.prompthub.domain.review.ReviewUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class ReviewModifyService implements ReviewRegister {
    private final ReviewRepository reviewRepository;
    private final ReviewFinder reviewFinder;
    private final PromptRegister promptRegister;

    @Override
    public Review register(ReviewRegisterRequest request, Prompt prompt, Member member) {
        // 1. 중복 리뷰 확인 (동일한 프롬프트에 이미 리뷰가 있는지)
        reviewRepository.findByPromptAndMember(prompt, member)
                .ifPresent(existingReview -> {
                    throw new IllegalArgumentException("이미 해당 프롬프트에 대한 리뷰를 작성했습니다.");
                });

        // 2. 리뷰 생성 및 저장
        Review review = reviewRepository.save(Review.register(request, prompt, member));

        // todo 이벤트 발행 방식으로 리팩토링하기
        // 3. 프롬프트 평점 업데이트
        promptRegister.addReview(prompt,  review);

        return review;
    }

    @Override
    public Review update(Long reviewId, ReviewUpdateRequest request, Member member) {
        // 1. 리뷰 조회
        Review review = reviewFinder.findWithMember(reviewId);

        // 2. 권한 확인 (본인이 작성한 리뷰인지)
        if (!review.isOwner(member)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        ReviewChange change = review.update(request);
        Review updatedReview = reviewRepository.save(review);

        // todo 이벤트 발행 방식으로 리팩토링하기
        // 평점이 변경된 경우에만 프롬프트 업데이트
        if (change.isRatingChanged()) {
            promptRegister.updateReview(
                    review.getPrompt(),
                    change.oldRating(),
                    change.newRating()
            );
        }

        return updatedReview;
    }

    @Override
    public void delete(Long reviewId, Member member) {
        // 1. 리뷰 조회
        Review review = reviewFinder.findWithMember(reviewId);

        // 2. 권한 확인 (본인이 작성한 리뷰인지)
        if (!review.isOwner(member)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // todo 이벤트 발행 방식으로 리팩토링하기
        // 3. 프롬프트에서 평점 제거
        promptRegister.removeReview(review.getPrompt(), review);

        // 4. 리뷰 삭제
        reviewRepository.delete(review);
    }
}
