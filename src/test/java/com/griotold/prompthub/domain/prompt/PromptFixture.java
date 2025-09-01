package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;

public class PromptFixture {

    public static PromptRegisterRequest createPromptRegisterRequest() {
        return new PromptRegisterRequest("제목제목제목", "컨텐츠컨텐츠컨텐츠", "설명설명설명");
    }

    public static PromptRegisterRequest createPromptRegisterRequest(String title, String content, String description) {
        return new PromptRegisterRequest(title, content, description);
    }

    public static PromptUpdateRequest createPromptUpdateRequest() {
        return new PromptUpdateRequest("수정제목", "수정컨텐츠", "수정설명");
    }

    public static  PromptUpdateRequest createPromptUpdateRequest(String title, String content, String description) {
        return new PromptUpdateRequest(title, content, description);
    }

    public static Prompt createPrompt() {
        MemberRegisterRequest memberRegisterRequest = MemberFixture.createMemberRegisterRequest();
        PasswordEncoder passwordEncoder = MemberFixture.createPasswordEncoder();
        return Prompt.register(createPromptRegisterRequest(),
                Member.register(memberRegisterRequest, passwordEncoder), CategoryFixture.createCategory());
    }

    public static Prompt createPrompt(String title, String content, String description) {
        MemberRegisterRequest memberRegisterRequest = MemberFixture.createMemberRegisterRequest();
        PasswordEncoder passwordEncoder = MemberFixture.createPasswordEncoder();
        return Prompt.register(createPromptRegisterRequest(title,  content, description),
                Member.register(memberRegisterRequest, passwordEncoder), CategoryFixture.createCategory());
    }

}
