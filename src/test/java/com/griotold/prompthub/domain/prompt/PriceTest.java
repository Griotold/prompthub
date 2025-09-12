package com.griotold.prompthub.domain.prompt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PriceTest {

    /**
     * 생성자
     */
    @Test
    void Price() {
        // when
        Price freePrice = new Price(0, false, 0);        // 무료
        Price paidPrice = new Price(5000, true, 3);      // 유료, 판매 3회
        Price newPaidPrice = new Price(1000, true, 0);   // 유료, 판매 0회

        // then
        assertThat(freePrice.amount()).isEqualTo(0);
        assertThat(freePrice.isPremium()).isFalse();
        assertThat(freePrice.salesCount()).isEqualTo(0);

        assertThat(paidPrice.amount()).isEqualTo(5000);
        assertThat(paidPrice.isPremium()).isTrue();
        assertThat(paidPrice.salesCount()).isEqualTo(3);

        assertThat(newPaidPrice.amount()).isEqualTo(1000);
        assertThat(newPaidPrice.isPremium()).isTrue();
        assertThat(newPaidPrice.salesCount()).isEqualTo(0);
    }

    @Test
    void Price_가격이_음수이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Price(-1, false, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 0 이상이어야 합니다.");
    }

    @Test
    void Price_판매수가_음수이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Price(1000, true, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("판매 수는 0 이상이어야 합니다.");
    }

    @Test
    void Price_가격이_0원인데_프리미엄이_true이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Price(0, true, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격과 프리미엄 여부가 일치하지 않습니다.");
    }

    @Test
    void Price_가격이_0원_초과인데_프리미엄이_false이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> new Price(1000, false, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격과 프리미엄 여부가 일치하지 않습니다.");
    }

    /**
     * free()
     */
    @Test
    void free() {
        // when
        Price price = Price.free();

        // then
        assertThat(price.amount()).isEqualTo(0);
        assertThat(price.isPremium()).isFalse();
        assertThat(price.salesCount()).isEqualTo(0);
    }

    @Test
    void free_여러번_호출해도_동일한_값() {
        // when
        Price price1 = Price.free();
        Price price2 = Price.free();

        // then
        assertThat(price1.amount()).isEqualTo(price2.amount());
        assertThat(price1.isPremium()).isEqualTo(price2.isPremium());
        assertThat(price1.salesCount()).isEqualTo(price2.salesCount());

        // record는 값이 같으면 동일한 객체로 판단
        assertThat(price1).isEqualTo(price2);
    }

    /**
     * premium()
     */
    @Test
    void premium() {
        // when
        Price price1 = Price.premium(1000);
        Price price2 = Price.premium(5000);
        Price price3 = Price.premium(10000);

        // then
        assertThat(price1.amount()).isEqualTo(1000);
        assertThat(price1.isPremium()).isTrue();
        assertThat(price1.salesCount()).isEqualTo(0);

        assertThat(price2.amount()).isEqualTo(5000);
        assertThat(price2.isPremium()).isTrue();
        assertThat(price2.salesCount()).isEqualTo(0);

        assertThat(price3.amount()).isEqualTo(10000);
        assertThat(price3.isPremium()).isTrue();
        assertThat(price3.salesCount()).isEqualTo(0);
    }

    @Test
    void premium_가격이_0이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> Price.premium(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유료 프롬프트는 가격이 0보다 커야 합니다.");
    }

    @Test
    void premium_가격이_음수이면_예외발생() {
        // when & then
        assertThatThrownBy(() -> Price.premium(-1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유료 프롬프트는 가격이 0보다 커야 합니다.");
    }

    /**
     * changeAmount()
     */
    @Test
    void changeAmount() {
        // given
        Price freePrice = Price.free();                    // 무료 (0원)
        Price paidPrice = new Price(5000, true, 3);       // 유료 5000원, 판매 3회
        Price anotherPaidPrice = new Price(2000, true, 1); // 유료 2000원, 판매 1회

        // when
        Price result1 = freePrice.changeAmount(3000);      // 무료 → 유료
        Price result2 = paidPrice.changeAmount(0);         // 유료 → 무료
        Price result3 = paidPrice.changeAmount(8000);      // 유료 → 다른 유료
        Price result4 = anotherPaidPrice.changeAmount(1000); // 유료 → 다른 유료

        // then
        assertThat(result1.amount()).isEqualTo(3000);
        assertThat(result1.isPremium()).isTrue();
        assertThat(result1.salesCount()).isEqualTo(0);      // 기존 salesCount 유지

        assertThat(result2.amount()).isEqualTo(0);
        assertThat(result2.isPremium()).isFalse();
        assertThat(result2.salesCount()).isEqualTo(3);      // 기존 salesCount 유지

        assertThat(result3.amount()).isEqualTo(8000);
        assertThat(result3.isPremium()).isTrue();
        assertThat(result3.salesCount()).isEqualTo(3);      // 기존 salesCount 유지

        assertThat(result4.amount()).isEqualTo(1000);
        assertThat(result4.isPremium()).isTrue();
        assertThat(result4.salesCount()).isEqualTo(1);      // 기존 salesCount 유지
    }

    @Test
    void changeAmount_가격이_음수이면_예외발생() {
        // given
        Price price = Price.premium(5000);

        // when & then
        assertThatThrownBy(() -> price.changeAmount(-1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가격은 0 이상이어야 합니다.");
    }

    /**
     * makeFree()
     */
    @Test
    void makeFree() {
        // given
        Price freePrice = Price.free();                    // 원래 무료
        Price paidPriceWithSales = new Price(5000, true, 3); // 유료, 판매 3회
        Price paidPriceWithoutSales = new Price(2000, true, 0); // 유료, 판매 0회

        // when
        Price result1 = freePrice.makeFree();              // 무료 → 무료
        Price result2 = paidPriceWithSales.makeFree();     // 유료(판매있음) → 무료
        Price result3 = paidPriceWithoutSales.makeFree();  // 유료(판매없음) → 무료

        // then
        assertThat(result1.amount()).isEqualTo(0);
        assertThat(result1.isPremium()).isFalse();
        assertThat(result1.salesCount()).isEqualTo(0);      // 원래 0 유지

        assertThat(result2.amount()).isEqualTo(0);
        assertThat(result2.isPremium()).isFalse();
        assertThat(result2.salesCount()).isEqualTo(3);      // ⭐ 과거 판매수 유지!

        assertThat(result3.amount()).isEqualTo(0);
        assertThat(result3.isPremium()).isFalse();
        assertThat(result3.salesCount()).isEqualTo(0);      // 원래 0 유지
    }

    @Test
    void makeFree_과거_판매실적이_있는_프롬프트도_무료_전환_가능() {
        // given - "베스트셀러였던 프롬프트를 무료로 공개" 시나리오
        Price bestSellerPrice = new Price(10000, true, 25); // 1만원, 25회 판매

        // when
        Price freePrice = bestSellerPrice.makeFree();

        // then
        assertThat(freePrice.amount()).isEqualTo(0);
        assertThat(freePrice.isPremium()).isFalse();
        assertThat(freePrice.salesCount()).isEqualTo(25);   // 🏆 판매 실적 보존!

        // 이제 무료이지만 "검증된 품질"임을 알 수 있음
        assertThat(freePrice.isFree()).isTrue();
        // salesCount > 0 이면 "과거에 검증받은 프롬프트"로 활용 가능
    }

    /**
     * increaseSales()
     */
    @Test
    void increaseSales() {
        // given
        Price newPaidPrice = Price.premium(5000);          // 새 유료 프롬프트 (판매 0회)
        Price soldPrice = new Price(3000, true, 5);        // 이미 5회 판매
        Price manySoldPrice = new Price(1000, true, 99);   // 99회 판매

        // when
        Price result1 = newPaidPrice.increaseSales();      // 첫 판매
        Price result2 = soldPrice.increaseSales();         // 추가 판매
        Price result3 = manySoldPrice.increaseSales();     // 100번째 판매

        // then
        assertThat(result1.amount()).isEqualTo(5000);      // 가격 유지
        assertThat(result1.isPremium()).isTrue();          // 프리미엄 상태 유지
        assertThat(result1.salesCount()).isEqualTo(1);     // 0 → 1

        assertThat(result2.amount()).isEqualTo(3000);      // 가격 유지
        assertThat(result2.isPremium()).isTrue();          // 프리미엄 상태 유지
        assertThat(result2.salesCount()).isEqualTo(6);     // 5 → 6

        assertThat(result3.amount()).isEqualTo(1000);      // 가격 유지
        assertThat(result3.isPremium()).isTrue();          // 프리미엄 상태 유지
        assertThat(result3.salesCount()).isEqualTo(100);   // 99 → 100 (기념비적!)
    }

    @Test
    void increaseSales_연속_판매_시뮬레이션() {
        // given
        Price initialPrice = Price.premium(2000);

        // when - 연속으로 3번 판매
        Price afterFirstSale = initialPrice.increaseSales();
        Price afterSecondSale = afterFirstSale.increaseSales();
        Price afterThirdSale = afterSecondSale.increaseSales();

        // then
        assertThat(initialPrice.salesCount()).isEqualTo(0);    // 원본 불변
        assertThat(afterFirstSale.salesCount()).isEqualTo(1);
        assertThat(afterSecondSale.salesCount()).isEqualTo(2);
        assertThat(afterThirdSale.salesCount()).isEqualTo(3);

        // 모든 단계에서 가격과 프리미엄 상태는 동일
        assertThat(afterThirdSale.amount()).isEqualTo(2000);
        assertThat(afterThirdSale.isPremium()).isTrue();
    }

    @Test
    void increaseSales_무료_프롬프트도_판매수_증가_가능() {
        // given - 과거에 유료였다가 무료로 전환된 프롬프트
        Price freePriceWithHistory = new Price(0, false, 5); // 무료이지만 과거 판매 5회

        // when - 이론적으로는 가능하지만 비즈니스적으로는 일어나지 않을 상황
        Price result = freePriceWithHistory.increaseSales();

        // then
        assertThat(result.amount()).isEqualTo(0);
        assertThat(result.isPremium()).isFalse();
        assertThat(result.salesCount()).isEqualTo(6);      // 6회로 증가
    }

    /**
     * isFree()
     */
    @Test
    void isFree() {
        // given
        Price freePrice = Price.free();                     // 무료 (0원)
        Price paidPrice = Price.premium(5000);              // 유료 (5000원)
        Price freePriceWithHistory = new Price(0, false, 3); // 무료이지만 과거 판매 이력 있음
        Price cheapPaidPrice = Price.premium(100);          // 저렴한 유료 (100원)

        // when & then
        assertThat(freePrice.isFree()).isTrue();            // 기본 무료 프롬프트
        assertThat(paidPrice.isFree()).isFalse();           // 유료 프롬프트
        assertThat(freePriceWithHistory.isFree()).isTrue(); // 과거 이력 있어도 현재 무료면 무료
        assertThat(cheapPaidPrice.isFree()).isFalse();      // 아무리 저렴해도 유료면 유료
    }

    @Test
    void isFree_유료에서_무료로_전환된_경우() {
        // given
        Price originalPaidPrice = Price.premium(10000);     // 원래 1만원
        Price soldPrice = new Price(10000, true, 15);       // 15회 판매 후

        // when
        Price convertedFreePrice = soldPrice.makeFree();    // 무료로 전환

        // then
        assertThat(originalPaidPrice.isFree()).isFalse();   // 원래는 유료
        assertThat(soldPrice.isFree()).isFalse();           // 전환 전에는 유료
        assertThat(convertedFreePrice.isFree()).isTrue();   // 전환 후에는 무료

        // 과거 판매 이력은 있지만 현재는 무료
        assertThat(convertedFreePrice.salesCount()).isEqualTo(15);
    }

    @Test
    void isFree_경계값_테스트() {
        // given
        Price zeroPrice = new Price(0, false, 0);           // 정확히 0원
        Price oneCentPrice = new Price(1, true, 0);         // 1원 (최소 유료)

        // when & then
        assertThat(zeroPrice.isFree()).isTrue();            // 0원은 무료
        assertThat(oneCentPrice.isFree()).isFalse();        // 1원이라도 유료는 유료
    }

    /**
     * isPremium()
     */
    @Test
    void isPremium() {
        // given
        Price freePrice = Price.free();                     // 무료 (0원)
        Price paidPrice = Price.premium(5000);              // 유료 (5000원)
        Price freePriceWithHistory = new Price(0, false, 3); // 무료이지만 과거 판매 이력 있음
        Price expensivePaidPrice = Price.premium(100000);   // 고가 유료 (10만원)

        // when & then
        assertThat(freePrice.isPremium()).isFalse();        // 기본 무료 프롬프트
        assertThat(paidPrice.isPremium()).isTrue();         // 유료 프롬프트
        assertThat(freePriceWithHistory.isPremium()).isFalse(); // 과거 이력 있어도 현재 무료면 일반
        assertThat(expensivePaidPrice.isPremium()).isTrue(); // 고가여도 유료면 프리미엄
    }

    @Test
    void isPremium_무료에서_유료로_전환된_경우() {
        // given
        Price originalFreePrice = Price.free();             // 원래 무료

        // when
        Price convertedPaidPrice = originalFreePrice.changeAmount(3000); // 3000원으로 전환

        // then
        assertThat(originalFreePrice.isPremium()).isFalse(); // 원래는 일반
        assertThat(convertedPaidPrice.isPremium()).isTrue(); // 전환 후에는 프리미엄

        // 판매 이력은 아직 없음
        assertThat(convertedPaidPrice.salesCount()).isEqualTo(0);
    }

    @Test
    void isPremium_경계값_테스트() {
        // given
        Price zeroPrice = new Price(0, false, 0);           // 정확히 0원
        Price oneCentPrice = new Price(1, true, 0);         // 1원 (최소 유료)

        // when & then
        assertThat(zeroPrice.isPremium()).isFalse();        // 0원은 일반
        assertThat(oneCentPrice.isPremium()).isTrue();      // 1원이라도 프리미엄
    }

    @Test
    void isPremium_과_isFree는_서로_반대() {
        // given
        Price freePrice = Price.free();
        Price paidPrice = Price.premium(5000);
        Price freePriceWithHistory = new Price(0, false, 10);

        // when & then - 무료와 프리미엄은 항상 반대
        assertThat(freePrice.isFree()).isNotEqualTo(freePrice.isPremium());
        assertThat(paidPrice.isFree()).isNotEqualTo(paidPrice.isPremium());
        assertThat(freePriceWithHistory.isFree()).isNotEqualTo(freePriceWithHistory.isPremium());

        // 더 명확하게
        assertThat(freePrice.isFree() && freePrice.isPremium()).isFalse(); // 둘 다 true일 수 없음
        assertThat(!paidPrice.isFree() && !paidPrice.isPremium()).isFalse(); // 둘 다 false일 수 없음
    }

    /**
     * calculateSellerRevenue()
     */
    @Test
    void calculateSellerRevenue() {
        // given
        Price noSalesPrice = Price.premium(1000);               // 1000원, 판매 0회
        Price oneSalePrice = new Price(1000, true, 1);          // 1000원, 판매 1회
        Price multipleSalesPrice = new Price(5000, true, 3);    // 5000원, 판매 3회
        Price freePriceWithHistory = new Price(0, false, 5);    // 0원, 과거 판매 5회

        // when & then
        assertThat(noSalesPrice.calculateSellerRevenue()).isEqualTo(0);      // 1000 * 0 * 0.8 = 0
        assertThat(oneSalePrice.calculateSellerRevenue()).isEqualTo(800);    // 1000 * 1 * 0.8 = 800
        assertThat(multipleSalesPrice.calculateSellerRevenue()).isEqualTo(12000); // 5000 * 3 * 0.8 = 12000
        assertThat(freePriceWithHistory.calculateSellerRevenue()).isEqualTo(0);   // 0 * 5 * 0.8 = 0
    }

    @Test
    void calculateSellerRevenue_소수점_절삭_테스트() {
        // given - 80% 계산 시 소수점이 발생하는 경우들
        Price price1 = new Price(1001, true, 1);    // 1001 * 1 * 0.8 = 800.8 → 800
        Price price2 = new Price(1002, true, 1);    // 1002 * 1 * 0.8 = 801.6 → 801
        Price price3 = new Price(1003, true, 1);    // 1003 * 1 * 0.8 = 802.4 → 802
        Price price4 = new Price(1009, true, 1);    // 1009 * 1 * 0.8 = 807.2 → 807

        // when & then - 소수점 절삭 확인
        assertThat(price1.calculateSellerRevenue()).isEqualTo(800);  // 0.8 절삭
        assertThat(price2.calculateSellerRevenue()).isEqualTo(801);  // 0.6 절삭
        assertThat(price3.calculateSellerRevenue()).isEqualTo(802);  // 0.4 절삭
        assertThat(price4.calculateSellerRevenue()).isEqualTo(807);  // 0.2 절삭
    }

    @Test
    void calculateSellerRevenue_다양한_판매_시나리오() {
        // given
        Price cheapItem = new Price(100, true, 10);     // 100원 * 10회 = 1000원 총매출
        Price moderateItem = new Price(5000, true, 7);  // 5000원 * 7회 = 35000원 총매출
        Price expensiveItem = new Price(50000, true, 2); // 50000원 * 2회 = 100000원 총매출

        // when & then
        assertThat(cheapItem.calculateSellerRevenue()).isEqualTo(800);      // 1000 * 0.8 = 800
        assertThat(moderateItem.calculateSellerRevenue()).isEqualTo(28000); // 35000 * 0.8 = 28000
        assertThat(expensiveItem.calculateSellerRevenue()).isEqualTo(80000); // 100000 * 0.8 = 80000
    }

    @Test
    void calculateSellerRevenue_판매자_수익률_80퍼센트_확인() {
        // given
        Price price = new Price(12500, true, 4);  // 총 매출: 50000원

        // when
        int sellerRevenue = price.calculateSellerRevenue();
        int totalRevenue = price.amount() * price.salesCount();

        // then
        assertThat(sellerRevenue).isEqualTo(40000);  // 50000 * 0.8 = 40000
        assertThat((double) sellerRevenue / totalRevenue).isEqualTo(0.8); // 정확히 80%
    }

    /**
     * calculatePlatformCommission()
     */
    @Test
    void calculatePlatformCommission() {
        // given
        Price noSalesPrice = Price.premium(1000);               // 1000원, 판매 0회
        Price oneSalePrice = new Price(1000, true, 1);          // 1000원, 판매 1회
        Price multipleSalesPrice = new Price(5000, true, 3);    // 5000원, 판매 3회
        Price freePriceWithHistory = new Price(0, false, 5);    // 0원, 과거 판매 5회

        // when & then
        assertThat(noSalesPrice.calculatePlatformCommission()).isEqualTo(0);      // 1000 * 0 * 0.2 = 0
        assertThat(oneSalePrice.calculatePlatformCommission()).isEqualTo(200);    // 1000 * 1 * 0.2 = 200
        assertThat(multipleSalesPrice.calculatePlatformCommission()).isEqualTo(3000); // 5000 * 3 * 0.2 = 3000
        assertThat(freePriceWithHistory.calculatePlatformCommission()).isEqualTo(0);   // 0 * 5 * 0.2 = 0
    }

    @Test
    void calculatePlatformCommission_소수점_절삭_테스트() {
        // given - 20% 계산 시 소수점이 발생하는 경우들
        Price price1 = new Price(1001, true, 1);    // 1001 * 1 * 0.2 = 200.2 → 200
        Price price2 = new Price(1007, true, 1);    // 1007 * 1 * 0.2 = 201.4 → 201
        Price price3 = new Price(1013, true, 1);    // 1013 * 1 * 0.2 = 202.6 → 202
        Price price4 = new Price(1019, true, 1);    // 1019 * 1 * 0.2 = 203.8 → 203

        // when & then - 소수점 절삭 확인
        assertThat(price1.calculatePlatformCommission()).isEqualTo(200);  // 0.2 절삭
        assertThat(price2.calculatePlatformCommission()).isEqualTo(201);  // 0.4 절삭
        assertThat(price3.calculatePlatformCommission()).isEqualTo(202);  // 0.6 절삭
        assertThat(price4.calculatePlatformCommission()).isEqualTo(203);  // 0.8 절삭
    }

    @Test
    void calculatePlatformCommission_다양한_판매_시나리오() {
        // given
        Price cheapItem = new Price(100, true, 10);     // 100원 * 10회 = 1000원 총매출
        Price moderateItem = new Price(5000, true, 7);  // 5000원 * 7회 = 35000원 총매출
        Price expensiveItem = new Price(50000, true, 2); // 50000원 * 2회 = 100000원 총매출

        // when & then
        assertThat(cheapItem.calculatePlatformCommission()).isEqualTo(200);      // 1000 * 0.2 = 200
        assertThat(moderateItem.calculatePlatformCommission()).isEqualTo(7000);  // 35000 * 0.2 = 7000
        assertThat(expensiveItem.calculatePlatformCommission()).isEqualTo(20000); // 100000 * 0.2 = 20000
    }

    @Test
    void calculatePlatformCommission_수수료율_20퍼센트_확인() {
        // given
        Price price = new Price(12500, true, 4);  // 총 매출: 50000원

        // when
        int platformCommission = price.calculatePlatformCommission();
        int totalRevenue = price.amount() * price.salesCount();

        // then
        assertThat(platformCommission).isEqualTo(10000);  // 50000 * 0.2 = 10000
        assertThat((double) platformCommission / totalRevenue).isEqualTo(0.2); // 정확히 20%
    }

    @Test
    void calculateSellerRevenue_와_calculatePlatformCommission_합계_검증() {
        // given
        Price price1 = new Price(5000, true, 3);   // 총 매출: 15000원
        Price price2 = new Price(1000, true, 7);   // 총 매출: 7000원
        Price price3 = new Price(25000, true, 2);  // 총 매출: 50000원

        // when
        int totalRevenue1 = price1.amount() * price1.salesCount();
        int sellerRevenue1 = price1.calculateSellerRevenue();
        int platformCommission1 = price1.calculatePlatformCommission();

        int totalRevenue2 = price2.amount() * price2.salesCount();
        int sellerRevenue2 = price2.calculateSellerRevenue();
        int platformCommission2 = price2.calculatePlatformCommission();

        int totalRevenue3 = price3.amount() * price3.salesCount();
        int sellerRevenue3 = price3.calculateSellerRevenue();
        int platformCommission3 = price3.calculatePlatformCommission();

        // then - 판매자 수익 + 플랫폼 수수료 = 총 매출 (소수점 절삭 오차 고려)
        assertThat(sellerRevenue1 + platformCommission1).isLessThanOrEqualTo(totalRevenue1);
        assertThat(sellerRevenue2 + platformCommission2).isLessThanOrEqualTo(totalRevenue2);
        assertThat(sellerRevenue3 + platformCommission3).isLessThanOrEqualTo(totalRevenue3);

        // 대부분의 경우 정확히 일치하거나 최대 1원 차이
        assertThat(totalRevenue1 - (sellerRevenue1 + platformCommission1)).isLessThanOrEqualTo(1);
        assertThat(totalRevenue2 - (sellerRevenue2 + platformCommission2)).isLessThanOrEqualTo(1);
        assertThat(totalRevenue3 - (sellerRevenue3 + platformCommission3)).isLessThanOrEqualTo(1);
    }
}