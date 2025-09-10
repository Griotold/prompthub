package com.griotold.prompthub.domain.inquiry;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryStatus {
    PENDING("대기중"),
    IN_PROGRESS("처리중"),
    RESOLVED("완료");

    private final String description;
}
