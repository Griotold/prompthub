package com.griotold.prompthub.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Provider {
    GOOGLE("구글"),
    NAVER("네이버"),
    KAKAO("카카오"),

    ;

    private final String description;
}
