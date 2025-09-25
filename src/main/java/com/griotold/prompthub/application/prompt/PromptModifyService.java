package com.griotold.prompthub.application.prompt;

import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.prompt.provided.PromptRegister;
import com.griotold.prompthub.application.prompt.provided.PromptTagFinder;
import com.griotold.prompthub.application.prompt.provided.PromptTagRegister;
import com.griotold.prompthub.application.prompt.required.PromptLikeRepository;
import com.griotold.prompthub.application.prompt.required.PromptRepository;
import com.griotold.prompthub.application.prompt.response.PromptDetailResponse;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptLike;
import com.griotold.prompthub.domain.prompt.PromptRegisterRequest;
import com.griotold.prompthub.domain.prompt.PromptUpdateRequest;
import com.griotold.prompthub.domain.review.Review;
import com.griotold.prompthub.domain.tag.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class PromptModifyService implements PromptRegister {
    private final PromptRepository promptRepository;
    private final PromptFinder promptFinder;
    private final PromptLikeRepository promptLikeRepository;
    private final PromptTagRegister promptTagRegister;
    private final PromptTagFinder promptTagFinder;

    @Override
    public PromptDetailResponse register(PromptRegisterRequest registerRequest, Member member, Category category) {
        Prompt prompt = promptRepository.save(Prompt.register(registerRequest, member, category));

        // 태그 연결
        List<Tag> linkedTags = List.of();
        if (registerRequest.hasValidTags()) {
            linkedTags = promptTagRegister.linkTagsByNames(prompt, registerRequest.getValidTags());
        }

        return PromptDetailResponse.of(prompt, false, linkedTags);
    }

    @Override
    public PromptDetailResponse update(Long promptId, PromptUpdateRequest request, Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);

        // 제목, 내용, 설명만 업데이트 (카테고리는 변경 불가)
        prompt.update(request);

        // 태그 업데이트
        List<Tag> updatedTags = updatePromptTags(prompt, request);

        promptRepository.save(prompt);
        boolean isLiked = promptFinder.isLikedBy(promptId, currentMember);

        return PromptDetailResponse.of(prompt, isLiked, updatedTags);
    }

    @Override
    public PromptDetailResponse makePrivate(Long promptId, Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);
        prompt.makePrivate();
        promptRepository.save(prompt);

        return createPromptDetailResponse(prompt, currentMember);
    }

    @Override
    public PromptDetailResponse makePublic(Long promptId, Member currentMember) {
        Prompt prompt = promptFinder.find(promptId);
        validateOwnership(prompt, currentMember);
        prompt.makePublic();
        promptRepository.save(prompt);

        return createPromptDetailResponse(prompt, currentMember);
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

    /**
     * 프롬프트의 태그를 업데이트한다
     * - 유효한 태그가 있으면 기존 태그를 모두 제거하고 새 태그로 교체
     * - 유효한 태그가 없으면 모든 태그 연결을 해제
     */
    private List<Tag> updatePromptTags(Prompt prompt, PromptUpdateRequest request) {
        if (request.hasValidTags()) {
            return promptTagRegister.updateTagsByNames(prompt, request.getValidTags());
        } else {
            promptTagRegister.unlinkAllTags(prompt);
            return List.of();
        }
    }

    /**
     * 프롬프트의 상세 응답을 생성하는 헬퍼 메서드
     */
    private PromptDetailResponse createPromptDetailResponse(Prompt prompt, Member currentMember) {
        boolean isLiked = promptFinder.isLikedBy(prompt.getId(), currentMember);
        List<Tag> tags = promptTagFinder.findTagsByPrompt(prompt);

        return PromptDetailResponse.of(prompt, isLiked, tags);
    }
}
