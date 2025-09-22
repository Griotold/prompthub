package com.griotold.prompthub.domain.prompt;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Price(
        @Column(name = "price", nullable = false, columnDefinition = "integer default 0")
        Integer amount,

        @Column(name = "is_premium", nullable = false, columnDefinition = "boolean default false")
        boolean isPremium,

        @Column(name = "sales_count", nullable = false, columnDefinition = "integer default 0")
        Integer salesCount
) {

    public Price {
        if (amount < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        if (salesCount < 0) {
            throw new IllegalArgumentException("판매 수는 0 이상이어야 합니다.");
        }
        // 비즈니스 로직 검증: 가격과 프리미엄 여부 일관성
        if ((amount > 0) != isPremium) {
            throw new IllegalArgumentException("가격과 프리미엄 여부가 일치하지 않습니다.");
        }
    }

    // 새 프롬프트 생성할 때, 기본적으로 무료
    public static Price free() {
        return new Price(0, false, 0);
    }

    // 유료 프롬프트로 생성
    public static Price premium(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("유료 프롬프트는 가격이 0보다 커야 합니다.");
        }
        return new Price(amount, true, 0);
    }

    /**
     * 가격을 변경합니다.
     * @param newAmount 새로운 가격 (0이면 무료로 전환)
     * @return 새로운 Price 인스턴스
     */
    public Price changeAmount(int newAmount) {
        if (newAmount < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다.");
        }
        return new Price(newAmount, newAmount > 0, this.salesCount);
    }

    /**
     * 무료로 전환합니다.
     * @return 새로운 Price 인스턴스
     */
    public Price makeFree() {
        return new Price(0, false, this.salesCount);
    }

    /**
     * 판매 횟수를 증가시킵니다.
     * @return 새로운 Price 인스턴스
     */
    public Price increaseSales() {
        return new Price(this.amount, this.isPremium, this.salesCount + 1);
    }

    /**
     * 무료 프롬프트인지 확인합니다.
     * @return 무료면 true
     */
    public boolean isFree() {
        return amount == 0;
    }

    /**
     * 프리미엄 프롬프트인지 확인합니다.
     * @return 유료면 true
     */
    public boolean isPremium() {
        return isPremium;
    }

    /**
     * 판매 수익을 계산합니다 (플랫폼 수수료 20% 제외).
     * @return 판매자 수취 총액
     */
    public int calculateSellerRevenue() {
        int totalRevenue = amount * salesCount;
        return (int) (totalRevenue * 0.8); // 80%가 판매자 몫
    }

    /**
     * 플랫폼 수수료를 계산합니다 (20%).
     * @return 플랫폼 수취 총액
     */
    public int calculatePlatformCommission() {
        int totalRevenue = amount * salesCount;
        return (int) (totalRevenue * 0.2); // 20%가 플랫폼 몫
    }
}