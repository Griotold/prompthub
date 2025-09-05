package com.griotold.prompthub.adapter.webapi.category;

import com.griotold.prompthub.adapter.security.user.LoginUser;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import com.griotold.prompthub.application.category.provided.CategoryFinder;
import com.griotold.prompthub.application.category.provided.CategoryRegister;
import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryRegisterRequest;
import com.griotold.prompthub.domain.category.CategoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryApi {

    private final CategoryFinder categoryFinder;
    private final CategoryRegister categoryRegister;

    /**
     * 카테고리 단건 조회 (인증 필요)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CategoryInfoResponse>> getCategory(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        log.info("카테고리 단건 조회. 카테고리 ID: {}, 사용자 ID: {}", id, loginUser.getMember().getId());

        Category category = categoryFinder.find(id);

        return BaseResponse.success(CategoryInfoResponse.of(category));
    }

    /**
     * 활성 카테고리 목록 조회 (인증 필요)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<CategoryInfoResponse>>> getActiveCategories(
            @AuthenticationPrincipal LoginUser loginUser) {
        log.info("활성 카테고리 목록 조회. 사용자 ID: {}", loginUser.getMember().getId());

        List<Category> categories = categoryFinder.findActiveOrderByName();
        List<CategoryInfoResponse> responses = categories.stream()
                .map(CategoryInfoResponse::of)
                .toList();

        return BaseResponse.success(responses);
    }

    /**
     * 카테고리 등록 (관리자만)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryInfoResponse>> createCategory(
            @RequestBody @Validated CategoryRegisterRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        log.info("카테고리 등록. 이름: {}, 관리자 ID: {}", request.name(), loginUser.getMember().getId());

        Category category = categoryRegister.register(request);

        return BaseResponse.success(CategoryInfoResponse.of(category));
    }

    /**
     * 카테고리 수정 (관리자만)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryInfoResponse>> updateCategory(
            @PathVariable("id") Long id,
            @RequestBody @Validated CategoryUpdateRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        log.info("카테고리 수정. 카테고리 ID: {}, 관리자 ID: {}", id, loginUser.getMember().getId());

        Category category = categoryRegister.updateInfo(id, request);

        return BaseResponse.success(CategoryInfoResponse.of(category));
    }

    /**
     * 카테고리 비활성화 (관리자만)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<CategoryInfoResponse>> deactivateCategory(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        log.info("카테고리 비활성화. 카테고리 ID: {}, 관리자 ID: {}", id, loginUser.getMember().getId());

        Category category = categoryRegister.deactivate(id);

        return BaseResponse.success(CategoryInfoResponse.of(category));
    }
}
