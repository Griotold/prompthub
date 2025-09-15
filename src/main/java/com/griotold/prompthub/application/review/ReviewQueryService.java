package com.griotold.prompthub.application.review;

import com.griotold.prompthub.application.review.provided.ReviewFinder;
import com.griotold.prompthub.application.review.required.ReviewRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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
}