package com.griotold.prompthub.adapter.security.social;

import com.griotold.prompthub.domain.member.Member;

public record MemberSaveResult(Member member, boolean isNewMember) {}
