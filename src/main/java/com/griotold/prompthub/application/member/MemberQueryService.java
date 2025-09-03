package com.griotold.prompthub.application.member;

import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService implements MemberFinder {

    private final MemberRepository memberRepository;

    @Override
    public Member find(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다. id: " + memberId));
    }

    public Member findById(Long memberId) {
        return find(memberId);
    }

    @Override
    public Optional<Member> findBySocial(Provider provider, String providerId) {
        return memberRepository.findByProviderAndProviderId(provider, providerId);
    }
}
