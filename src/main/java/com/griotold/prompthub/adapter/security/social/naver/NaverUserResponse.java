package com.griotold.prompthub.adapter.security.social.naver;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NaverUserResponse(
        @JsonProperty("resultcode")
        String resultCode,

        String message,

        NaverUserInfo response
) {
    public record NaverUserInfo(
            String id,
            String nickname,
            String email,

            @JsonProperty("profile_image")
            String profileImage

    ) {}

    // 구글/카카오와 동일한 방식으로 접근할 수 있는 편의 메서드
    public String getId() {
        return response != null ? response.id : null;
    }

    public String getEmail() {
        return response != null ? response.email : null;
    }

    public String getNickname() {
        return response != null ? response.nickname : null;
    }


    public String getProfileImage() {
        return response != null ? response.profileImage : null;
    }
}
