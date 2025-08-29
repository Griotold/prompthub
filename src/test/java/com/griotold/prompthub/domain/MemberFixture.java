package com.griotold.prompthub.domain;

public class MemberFixture {

    public static PasswordEncoder createPasswordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(String password) {
                return password.toUpperCase();
            }

            @Override
            public boolean matches(String password, String passwordHash) {
                return encode(password).equals(passwordHash);
            }
        };
    }

    public static MemberRegisterRequest createMemberRegisterRequest(String password, String passwordCheck) {
        return new MemberRegisterRequest("rio@prompthub.app", password, passwordCheck, "nickname");
    }

    public static MemberRegisterRequest createMemberRegisterRequest(String email) {
        return new MemberRegisterRequest(email, "secret3456", "secret3456", "nickname");
    }

    public static MemberRegisterRequest createMemberRegisterRequest() {
        return createMemberRegisterRequest("rio@prompthub.app");
    }
}
