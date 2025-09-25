package com.griotold.prompthub.domain.prompt;

import com.griotold.prompthub.domain.category.Category;
import com.griotold.prompthub.domain.category.CategoryFixture;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.MemberFixture;
import com.griotold.prompthub.domain.member.MemberRegisterRequest;
import com.griotold.prompthub.domain.member.PasswordEncoder;

import java.util.List;

public class PromptFixture {

    public static PromptRegisterRequest createPromptRegisterRequest() {
        return new PromptRegisterRequest("제목제목제목", "컨텐츠컨텐츠컨텐츠", "설명설명설명", 1L, null);
    }

    public static PromptRegisterRequest createPromptRegisterRequest(String title, String content, String description) {
        return new PromptRegisterRequest(title, content, description, 1L, null);
    }

    public static PromptRegisterRequest createPromptRegisterRequest(String title, String content, String description, List<String> tags) {
        return new PromptRegisterRequest(title, content, description, 1L, tags);
    }

    public static PromptRegisterRequest createPromptRegisterRequestWithTags(List<String> tags) {
        return new PromptRegisterRequest("제목제목제목", "컨텐츠컨텐츠컨텐츠", "설명설명설명", 1L, tags);
    }

    // 기존 메서드들 수정 (tags 필드 추가)
    public static PromptUpdateRequest createPromptUpdateRequest() {
        return new PromptUpdateRequest("수정제목", "수정컨텐츠", "수정설명", null);
    }

    public static PromptUpdateRequest createPromptUpdateRequest(String title, String content, String description) {
        return new PromptUpdateRequest(title, content, description, null);
    }

    // 태그를 포함한 버전들 추가 (RegisterRequest와 동일한 패턴)
    public static PromptUpdateRequest createPromptUpdateRequest(String title, String content, String description, List<String> tags) {
        return new PromptUpdateRequest(title, content, description, tags);
    }

    public static PromptUpdateRequest createPromptUpdateRequestWithTags(List<String> tags) {
        return new PromptUpdateRequest("수정제목", "수정컨텐츠", "수정설명", tags);
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

    public static Prompt createPrompt(Member member, Category category) {
        return Prompt.register(createPromptRegisterRequest(), member, category);
    }

    public static Prompt createAnotherPrompt(Member member, Category category) {
        return Prompt.register(
                createPromptRegisterRequest("다른 제목", "컨텐츠컨텐츠컨텐츠", "설명설명설명"),
                member,
                category
        );
    }

}
