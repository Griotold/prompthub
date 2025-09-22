package com.griotold.prompthub.adapter.security;

import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.adapter.webapi.auth.LoginResponse;
import com.griotold.prompthub.adapter.webapi.admin.AdminLoginRequest;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.domain.member.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminAuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(AdminLoginRequest request) {
        log.info("관리자 로그인 시도: {}", request.nickname());

        Member admin = findAdminByNickname(request.nickname());

        validateAdminCredentials(admin, request.password());

        LoginResponse response = generateTokenResponse(admin);

        log.info("관리자 로그인 성공: {}", request.nickname());
        return response;
    }

    private Member findAdminByNickname(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다"));
    }

    private void validateAdminCredentials(Member member, String password) {
        validateAdminRole(member);
        validateAccountStatus(member);
        validatePassword(member, password);
    }

    private void validateAdminRole(Member member) {
        if (member.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다");
        }
    }

    private void validateAccountStatus(Member member) {
        if (!member.isActive()) {
            throw new IllegalArgumentException("비활성화된 계정입니다");
        }
    }

    private void validatePassword(Member member, String password) {
        if (!member.verifyPassword(password, passwordEncoder)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
    }

    private LoginResponse generateTokenResponse(Member admin) {
        String accessToken = jwtTokenProvider.createAccessToken(admin);
        String refreshToken = jwtTokenProvider.createRefreshToken(admin.getId());
        return new LoginResponse(accessToken, refreshToken);
    }
}