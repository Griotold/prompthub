package com.griotold.prompthub.domain.payment;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.prompt.Prompt;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Getter
@Table(name = "p_payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_buyer_prompt_payment", columnNames = {"buyer_id", "prompt_id"})
        })
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prompt_id", nullable = false)
    private Prompt prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Member buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @Column(nullable = false)
    private Integer purchasePrice;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(length = 200)
    private String paymentKey;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.COMPLETED;

    @CreatedDate
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 프롬프트 구매 결제 생성
    public static Payment createPromptPurchase(Prompt prompt, Member buyer, PaymentMethod paymentMethod, String paymentKey) {
        requireNonNull(prompt, "Prompt cannot be null");
        requireNonNull(buyer, "Buyer cannot be null");
        requireNonNull(paymentMethod, "PaymentMethod cannot be null");

        if (prompt.isFree()) {
            throw new IllegalArgumentException("Cannot purchase a free prompt");
        }

        if (prompt.isOwnedBy(buyer)) {
            throw new IllegalArgumentException("Cannot purchase own prompt");
        }

        Payment payment = new Payment();
        payment.prompt = prompt;
        payment.buyer = buyer;
        payment.seller = prompt.getMember();
        payment.purchasePrice = prompt.getPriceAmount();
        payment.paymentMethod = paymentMethod;
        payment.paymentKey = paymentKey;
        payment.paymentStatus = PaymentStatus.COMPLETED;

        return payment;
    }

    // 결제 상태 변경
    public void updatePaymentStatus(PaymentStatus status) {
        this.paymentStatus = requireNonNull(status);
    }

    // 결제 완료 여부 확인
    public boolean isCompleted() {
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    // 환불 가능 여부 확인 (예: 7일 이내)
    public boolean isRefundable() {
        if (paymentStatus != PaymentStatus.COMPLETED) {
            return false;
        }
        return purchasedAt.plusDays(7).isAfter(LocalDateTime.now());
    }

    // 환불 처리
    public void refund() {
        if (!isRefundable()) {
            throw new IllegalStateException("This payment is not refundable");
        }
        this.paymentStatus = PaymentStatus.REFUNDED;
    }

    // 판매자 수익 계산 (수수료 제외)
    public Integer getSellerRevenue() {
        if (!isCompleted()) {
            return 0;
        }
        return (int) (purchasePrice * 0.85); // 15% 플랫폼 수수료
    }

    // 플랫폼 수수료 계산
    public Integer getPlatformCommission() {
        if (!isCompleted()) {
            return 0;
        }
        return (int) (purchasePrice * 0.15);
    }

    // 프롬프트 구매 결제 여부 확인
    public boolean isPromptPurchase() {
        return prompt != null;
    }
}