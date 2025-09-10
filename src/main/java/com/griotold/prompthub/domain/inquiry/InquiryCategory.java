package com.griotold.prompthub.domain.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryCategory {
    TECHNICAL("기술지원"),
    ACCOUNT("계정문의"),
    ETC("기타");

    private final String description;
}
