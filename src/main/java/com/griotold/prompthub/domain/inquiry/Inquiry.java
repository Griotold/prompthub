package com.griotold.prompthub.domain.inquiry;

import com.griotold.prompthub.domain.AbstractEntity;
import com.griotold.prompthub.domain.member.Member;
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
@Table(name = "p_inquiry")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Inquiry extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")  // nullable = true (비회원도 문의 가능)
    private Member member;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private InquiryCategory inquiryCategory;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(length = 150)
    private String contactEmail; // 비회원 이메일

    @Column(length = 100)
    private String contactName; // 비회원 이름

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    @Column(length = 3000)
    private String adminReply; // 관리자 답변

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt; // 완료 시간

    public static Inquiry register(InquiryRegisterRequest request, Member member) {
        Inquiry inquiry = new Inquiry();
        inquiry.member = member; // 회원은 null 가능
        inquiry.inquiryCategory = requireNonNull(request.inquiryCategory());
        inquiry.title = requireNonNull(request.title());
        inquiry.content = requireNonNull(request.content());
        inquiry.contactEmail = request.contactEmail();
        inquiry.contactName = request.contactName();
        inquiry.status = InquiryStatus.PENDING;

        return inquiry;
    }

//    public void updateStatus(InquiryUpdateRequest request) {
//        this.status = requireNonNull(request.status());
//        this.adminReply = request.adminReply();
//
//        if (this.status == InquiryStatus.RESOLVED) {
//            this.resolvedAt = LocalDateTime.now();
//        }
//    }
}

