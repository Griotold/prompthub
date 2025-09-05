package com.griotold.prompthub.adapter.webapi.member;

import com.griotold.prompthub.adapter.security.user.LoginUser;
import com.griotold.prompthub.adapter.webapi.dto.BaseResponse;
import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberApi {

    private final MemberFinder memberFinder;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MemberProfileResponse>> getMyProfile(@AuthenticationPrincipal LoginUser loginUser) {
        log.info("사용자 프로필 조회. id: {}", loginUser.getMember().getId());

        Member member = memberFinder.find(loginUser.getMember().getId());

        return BaseResponse.success(MemberProfileResponse.of(member));
    }
}
