package com.griotold.prompthub.domain.member;

import com.griotold.prompthub.domain.AbstractEntity;
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
import static org.springframework.util.Assert.state;

@Entity
@Getter
@Table(name = "p_member", uniqueConstraints = {
        @UniqueConstraint(name = "uk_email_provider", columnNames = {"email_address", "provider"})
})
// @ToString(callSuper = true, exclude = "detail")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member extends AbstractEntity {

    @Embedded
    private Email email;

    @Column(length = 100, nullable = false)
    private String nickname;

    @Column(length = 200)
    private String passwordHash;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(length = 100)
    private String providerId;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @CreatedDate
    private LocalDateTime registeredAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deactivatedAt;

    public static Member register(MemberRegisterRequest registerRequest, PasswordEncoder passwordEncoder) {
        state(registerRequest.password().equals(registerRequest.passwordCheck()), "비밀번호와 비밀번호 확인이 일치하지 않습니다");

        Member member = new Member();

        member.email = new Email(registerRequest.email(), false);
        member.nickname = requireNonNull(registerRequest.nickName());
        member.passwordHash = requireNonNull(passwordEncoder.encode(registerRequest.password()));

        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;

        return member;
    }

    // todo profileImageUrl 넣는 거 해야 함.
    public static Member registerWithSocial(SocialRegisterRequest request) {
        Member member = new Member();

        member.email = new Email(request.email(), true);
        member.nickname = requireNonNull(request.nickname());
        member.passwordHash = "SOCIAL_LOGIN";
        member.provider = request.provider();
        member.providerId = request.providerId();
        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;

        return member;
    }

    // Member.java에 추가
    public static Member createAdmin(String email, String password, String nickname, PasswordEncoder passwordEncoder) {
        Member admin = new Member();

        admin.email = new Email(email, true); // 관리자는 이메일 검증됨
        admin.nickname = requireNonNull(nickname);
        admin.passwordHash = requireNonNull(passwordEncoder.encode(password));
        admin.provider = null; // 소셜 로그인 아님
        admin.providerId = null;
        admin.role = Role.ADMIN; // 핵심: ADMIN 역할 설정
        admin.status = MemberStatus.ACTIVE;

        return admin;
    }

    public void deactivate() {
        state(status == MemberStatus.ACTIVE, "MemberStatus is Not Active");

        this.status = MemberStatus.DEACTIVATED;
        this.deactivatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        this.status = MemberStatus.ACTIVE;
        this.deactivatedAt = null;
    }

    public boolean verifyPassword (String password, PasswordEncoder passwordEncoder) {
        // 소셜 로그인 사용자는 비밀번호 검증 불가
        if (isSocialUser()) {
            return false;
        }
        return passwordEncoder.matches(password, passwordHash);
    }

    public boolean isSocialUser() {
        return provider != null && providerId != null;
    }

    public void verifyEmail() {
        this.email = new Email(this.email.address(), true);
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isEmailVerified() {
        return this.email.verified();
    }

    private void setProfileImageUrlIfValid(String profileImageUrl) {
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
