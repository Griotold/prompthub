package com.griotold.prompthub.adapter.webapi.prompt;


import com.griotold.prompthub.adapter.security.user.LoginUser;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import com.griotold.prompthub.adapter.webapi.dto.PageResponse;
import com.griotold.prompthub.application.category.provided.CategoryFinder;
import com.griotold.prompthub.application.prompt.provided.PromptFinder;
import com.griotold.prompthub.application.prompt.provided.PromptRegister;
import com.griotold.prompthub.application.prompt.response.PromptDetailResponse;
import com.griotold.prompthub.application.prompt.response.PromptListResponse;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.prompt.Prompt;
import com.griotold.prompthub.domain.prompt.PromptRegisterRequest;
import com.griotold.prompthub.domain.prompt.PromptUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/prompts")
@RequiredArgsConstructor
public class PromptApi {

    private final PromptFinder promptFinder;
    private final PromptRegister promptRegister;
    private final CategoryFinder categoryFinder;


    /**
     * 프롬프트 목록 조회 (공개된 것만, 페이징)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<PromptListResponse>>> getPrompts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 목록 조회. 카테고리: {}, 키워드: {}", categoryId, keyword);

        Page<PromptListResponse> responses = promptFinder.findPublicPrompts(categoryId, keyword, pageable);
        return BaseResponse.success(PageResponse.of(responses));
    }

    /**
     * 인기 프롬프트 목록 조회 (별도 엔드포인트)
     */
    @GetMapping("/popular")
    public ResponseEntity<BaseResponse<PageResponse<PromptListResponse>>> getPopularPrompts(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("인기 프롬프트 목록 조회");

        Page<PromptListResponse> responses = promptFinder.findPopularPrompts(pageable);
        return BaseResponse.success(PageResponse.of(responses));
    }

    /**
     * 프롬프트 상세 조회 (조회수 증가)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PromptDetailResponse>> getPrompt(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 상세 조회. ID: {}, 사용자: {}", id, loginUser.getMember().getId());

        // 조회수 증가
        return BaseResponse.success(promptFinder.getPromptDetail(id, loginUser.getMember()));
    }

    /**
     * 프롬프트 등록
     */
    @PostMapping
    public ResponseEntity<BaseResponse<PromptDetailResponse>> register(
            @RequestBody @Validated PromptRegisterRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 등록. 제목: {}, 작성자: {}", request.title(), loginUser.getMember().getId());

        Category category = categoryFinder.find(request.categoryId());
        PromptDetailResponse response = promptRegister.register(request, loginUser.getMember(), category);

        return BaseResponse.success(response);
    }

    /**
     * 프롬프트 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<PromptDetailResponse>> update(
            @PathVariable Long id,
            @RequestBody @Validated PromptUpdateRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 수정. ID: {}, 작성자: {}", id, loginUser.getMember().getId());

        PromptDetailResponse response = promptRegister.update(id, request, loginUser.getMember());

        return BaseResponse.success(response);
    }

    /**
     * 프롬프트 삭제 (비공개 처리)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<PromptDetailResponse>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 삭제. ID: {}, 작성자: {}", id, loginUser.getMember().getId());

        PromptDetailResponse response = promptRegister.makePrivate(id, loginUser.getMember());

        return BaseResponse.success(response);
    }

    /**
     * 프롬프트 복구 (공개 처리)
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<BaseResponse<PromptDetailResponse>> restore(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("프롬프트 복구. ID: {}, 작성자: {}", id, loginUser.getMember().getId());

        PromptDetailResponse response = promptRegister.makePublic(id, loginUser.getMember());

        return BaseResponse.success(response);
    }

    /**
     * 좋아요 토글
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<BaseResponse<Void>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("좋아요 토글. 프롬프트 ID: {}, 사용자: {}", id, loginUser.getMember().getId());

        boolean isLiked = promptFinder.isLikedBy(id, loginUser.getMember());

        if (isLiked) {
            promptRegister.removeLike(id, loginUser.getMember());
        } else {
            promptRegister.addLike(id, loginUser.getMember());
        }

        return BaseResponse.success(null);
    }

    /**
     * 내가 작성한 프롬프트 목록
     */
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<PageResponse<PromptListResponse>>> getMyPrompts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal LoginUser loginUser) {

        Page<PromptListResponse> responses = promptFinder.findAllByMember(loginUser.getMember(), pageable);
        return BaseResponse.success(PageResponse.of(responses));
    }

    /**
     * 좋아요한 프롬프트 목록 (PromptFinder에 메서드 추가 필요)
     */
    @GetMapping("/liked")
    public ResponseEntity<BaseResponse<PageResponse<PromptListResponse>>> getLikedPrompts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal LoginUser loginUser) {

        Page<Prompt> prompts = promptFinder.findLikedByMember(loginUser.getMember(), pageable);
        Page<PromptListResponse> responses = prompts.map(PromptListResponse::of);

        return BaseResponse.success(PageResponse.of(responses));
    }
}
