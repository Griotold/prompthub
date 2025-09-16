package com.griotold.prompthub.application.prompt;

import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.prompt.provided.PromptRegister;
import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptLike;
import com.griotold.prompthub.domain.prompt.PromptRegisterRequest;
import com.griotold.prompthub.domain.prompt.PromptUpdateRequest;
import com.griotold.prompthub.domain.review.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class PromptModifyService implements PromptRegister {
    private final PromptRepository promptRepository;
    private final PromptFinder promptFinder;
    private final PromptLikeRepository promptLikeRepository;

    @Override
    public Prompt register(PromptRegisterRequest registerRequest, Member member, Category category) {
        return promptRepository.save(Prompt.register(registerRequest, member, category));
    }

    // todo 변경 할 때, 카테고리 변경이랑 통합해야하는 것이 아닌가?
    // 프롬프트 수정 시 트랜잭션 최적화
    // 현재 updateInfo와 changeCategory가 분리되어 있어 두 번의 DB 접근 발생 가능
    // 통합 메서드로 한 번의 트랜잭션에서 모든 수정 처리하도록 개선
    @Override
    public Prompt updateInfo(Long promptId, PromptUpdateRequest request,  Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);
        prompt.update(request);
        return promptRepository.save(prompt);
    }

    @Override
    public Prompt makePublic(Long promptId, Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);
        prompt.makePublic();
        return promptRepository.save(prompt);
    }

    @Override
    public Prompt makePrivate(Long promptId, Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);
        prompt.makePrivate();
        return promptRepository.save(prompt);
    }

    @Override
    public Prompt increaseViewCount(Long promptId) {
        Prompt prompt = promptFinder.find(promptId);
        prompt.increaseViewCount();
        return promptRepository.save(prompt);
    }

    @Override
    public void addLike(Long promptId, Member member) {
        Prompt prompt = promptFinder.find(promptId);

        // 이미 좋아요한 경우 예외 처리
        if (promptLikeRepository.existsByPromptAndMember(prompt, member)) {
            throw new IllegalStateException("이미 좋아요한 프롬프트입니다.");
        }

        // 좋아요 추가 및 카운트 증가
        promptLikeRepository.save(PromptLike.create(member, prompt));
        prompt.increaseLikeCount();
        promptRepository.save(prompt);
    }

    @Override
    public void removeLike(Long promptId, Member member) {
        Prompt prompt = promptFinder.find(promptId);

        // 좋아요하지 않은 경우 예외 처리
        if (!promptLikeRepository.existsByPromptAndMember(prompt, member)) {
            throw new IllegalStateException("좋아요하지 않은 프롬프트입니다.");
        }

        // 좋아요 삭제 및 카운트 감소
        promptLikeRepository.deleteByPromptAndMember(prompt, member);
        prompt.decreaseLikeCount();
        promptRepository.save(prompt);
    }

    @Override
    public void addReview(Prompt prompt, Review review) {
        prompt.addRating(review);
        promptRepository.save(prompt);
    }

    @Override
    public void updateReview(Prompt prompt, Integer oldRating, Integer newRating) {
        prompt.removeRating(oldRating);
        prompt.addRating(newRating);
        promptRepository.save(prompt);
    }

    @Override
    public void removeReview(Prompt prompt, Review review) {
        prompt.removeRating(review);
        promptRepository.save(prompt);
    }

    private void validateOwnership(Prompt prompt, Member currentMember) {
        if (!prompt.getMember().getId().equals(currentMember.getId())) {
            throw new IllegalArgumentException("본인이 작성한 프롬프트만 수정할 수 있습니다.");
        }
    }
}
