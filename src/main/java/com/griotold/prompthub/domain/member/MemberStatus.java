package com.griotold.prompthub.domain.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberStatus {
    ACTIVE("활성화"),
    DEACTIVATED("비활성화"),

    ;
    private final String description;
}
