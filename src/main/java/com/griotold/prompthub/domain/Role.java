package com.griotold.prompthub.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER("회원"),
    ADMIN("관리자"),
    ;

    private final String description;
}
