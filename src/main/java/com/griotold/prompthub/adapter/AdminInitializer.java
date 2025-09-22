package com.griotold.prompthub.adapter;

import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;
import com.griotold.prompthub.domain.member.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Profile("!test") // 테스트 환경 제외
@Slf4j
@RequiredArgsConstructor
public class AdminInitializer {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.nickname}")
    private String adminNickname;

    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdmin() {
        log.info("AdminInitializer 실행 시작");

        // 특정 닉네임의 관리자 계정이 있는지 확인
        boolean adminExists = memberRepository.findByNickname(adminNickname).isPresent();
        log.info("기본 관리자 계정({}) 존재 여부: {}", adminNickname, adminExists);

        if (!adminExists) {
            log.info("기본 관리자 계정 생성 시작 - email: {}, nickname: {}", adminEmail, adminNickname);

            Member admin = Member.createAdmin(
                    adminEmail,
                    adminPassword,
                    adminNickname,
                    passwordEncoder
            );

            Member saved = memberRepository.save(admin);
            log.info("기본 관리자 계정 생성 완료: {} (ID: {}, Role: {})",
                    adminEmail, saved.getId(), saved.getRole());
        } else {
            log.info("기본 관리자 계정({})이 이미 존재합니다.", adminNickname);
        }
    }
}
