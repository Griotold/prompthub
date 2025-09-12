package com.griotold.prompthub.domain.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    TOSS_PAY("토스페이"),
    KAKAO_PAY("카카오페이"),
    NAVER_PAY("네이버페이");

    private final String description;
}
