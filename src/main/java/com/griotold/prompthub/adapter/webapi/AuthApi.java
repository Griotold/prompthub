package com.griotold.prompthub.adapter.webapi;

import com.griotold.prompthub.adapter.security.jwt.RefreshTokenService;
import com.griotold.prompthub.adapter.security.user.LoginUser;
import com.griotold.prompthub.adapter.security.social.google.GoogleAuthService;
import com.griotold.prompthub.adapter.security.social.TokenResponse;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import com.griotold.prompthub.adapter.webapi.dto.request.LoginRequest;
import com.griotold.prompthub.adapter.webapi.dto.request.RefreshTokenRequest;
import com.griotold.prompthub.adapter.webapi.dto.response.LoginResponse;
import com.griotold.prompthub.adapter.webapi.dto.response.RefreshTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthApi {

    private final GoogleAuthService googleAuthService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 구글 OAuth2 로그인
     */
    @PostMapping("/google/login")
    public ResponseEntity<BaseResponse<LoginResponse>> googleLogin(@RequestBody @Valid LoginRequest request) {

        log.info("구글 로그인 요청 - 인가코드: {}", request.authorizationCode());

        TokenResponse tokenResponse = googleAuthService.login(request.authorizationCode());

        LoginResponse response = new LoginResponse(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken()
        );

        log.info("구글 로그인 성공");

        // isNewMember에 따라 상태코드 분기
        if (tokenResponse.isNewMember()) {
            return BaseResponse.created(response);  // 201 Created
        } else {
            return BaseResponse.success(response);  // 200 OK
        }
    }

    /**
     * 리프레시 토큰으로 액세스 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<RefreshTokenResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request,
            @AuthenticationPrincipal LoginUser loginUser
    ) {
        log.info("토큰 갱신 요청 - 사용자: {}", loginUser.getUsername());

        RefreshTokenResponse response = refreshTokenService.refreshToken(
                request.refreshToken(),
                loginUser.getMember().getId()
        );

        log.info("토큰 갱신 성공 - 사용자: {}", loginUser.getUsername());
        return BaseResponse.success(response);
    }
}
