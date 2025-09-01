package com.griotold.prompthub.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.Assert.state;

@Entity
@Getter
@Table(name = "p_member")
// @ToString(callSuper = true, exclude = "detail")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member extends AbstractEntity {

    @Embedded
    @NaturalId
    private Email email;

    @Column(length = 100, nullable = false)
    private String nickname;

    @Column(length = 200, nullable = false)
    private String passwordHash;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreatedDate
    private LocalDateTime registeredAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime deactivatedAt;

    public static Member register(MemberRegisterRequest registerRequest, PasswordEncoder passwordEncoder) {
        state(registerRequest.password().equals(registerRequest.passwordCheck()), "비밀번호와 비밀번호 확인이 일치하지 않습니다");

        Member member = new Member();

        member.email = new Email(registerRequest.email());
        member.nickname = requireNonNull(registerRequest.nickName());
        member.passwordHash = requireNonNull(passwordEncoder.encode(registerRequest.password()));

        member.role = Role.USER;
        member.status = MemberStatus.ACTIVE;
        member.emailVerified = false;

        return member;
    }

    public void deactivate() {
        state(status == MemberStatus.ACTIVE, "MemberStatus is Not Active");

        this.status = MemberStatus.DEACTIVATED;
        this.deactivatedAt = LocalDateTime.now();
    }

    public boolean verifyPassword (String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, passwordHash);
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }
}
