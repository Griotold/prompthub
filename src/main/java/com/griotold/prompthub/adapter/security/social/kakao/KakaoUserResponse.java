package com.griotold.prompthub.adapter.security.social.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            Profile profile,
            String email
    ) {}

    public record Profile(
            String nickname,
            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {}

    // 편의 메서드들
    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email : null;
    }

    public String getNickname() {
        return kakaoAccount != null && kakaoAccount.profile != null
                ? kakaoAccount.profile.nickname : null;
    }

    public String getProfileImageUrl() {
        return kakaoAccount != null && kakaoAccount.profile != null
                ? kakaoAccount.profile.profileImageUrl : null;
    }
}