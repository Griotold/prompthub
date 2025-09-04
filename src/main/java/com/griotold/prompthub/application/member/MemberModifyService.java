package com.griotold.prompthub.application.member;

import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.application.member.provided.MemberRegister;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.SocialRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
@Validated
public class MemberModifyService implements MemberRegister {

    private final MemberRepository memberRepository;
    private final MemberFinder memberFinder;

    @Override
    public Member registerWithSocial(SocialRegisterRequest registerRequest) {
        return memberRepository.save(Member.registerWithSocial(registerRequest));
    }

    @Override
    public Member reactivate(Long memberId) {
        Member member = memberFinder.find(memberId);
        member.reactivate();
        return memberRepository.save(member);
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }
}
