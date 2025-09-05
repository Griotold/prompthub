package com.griotold.prompthub.adapter.webapi.member;

import com.griotold.prompthub.domain.member.Member;

public record MemberProfileResponse(
        String email,
        String nickname
) {

    public static MemberProfileResponse of(Member member) {
        return new MemberProfileResponse(member.getEmail().address(), member.getNickname());
    }
}
