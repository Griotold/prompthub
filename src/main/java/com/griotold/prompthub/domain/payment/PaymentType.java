package com.griotold.prompthub.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


// todo 나중에 Payment에 추가 예정
// 지금은 유료도 없고
@Getter
@RequiredArgsConstructor
public enum PaymentType {
    PROMPT_PURCHASE("프롬프트 구매"),
    SUBSCRIPTION("구독"),
    CREDIT_CHARGE("크레딧 충전"),
    TIP("후원");

    private final String description;
}