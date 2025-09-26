package com.griotold.prompthub.adapter.webapi.admin;

import com.griotold.prompthub.adapter.security.AdminAuthService;
import com.griotold.prompthub.adapter.webapi.auth.LoginResponse;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthApi {

    private final AdminAuthService adminAuthService;

    /**
     * 관리자 로그인
     */
    @Operation(summary = "관리자 로그인")
    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @RequestBody @Validated AdminLoginRequest request) {

        log.info("관리자 로그인 요청. 닉네임: {}", request.nickname());

        LoginResponse response = adminAuthService.login(request);

        log.info("관리자 로그인 성공. 닉네임: {}", request.nickname());
        return BaseResponse.success(response);
    }
}
