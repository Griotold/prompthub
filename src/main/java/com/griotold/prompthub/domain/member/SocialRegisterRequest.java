package com.griotold.prompthub.domain.member;

public record SocialRegisterRequest(
        String email,
        String nickname,
        Provider provider,
        String providerId
) {}
