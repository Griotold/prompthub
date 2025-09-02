package com.griotold.prompthub.domain.member;

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

    public static MemberRegisterRequest createMemberRegisterRequest(String email, String password, String passwordCheck, String nickname) {
        return new MemberRegisterRequest(email, password, passwordCheck, nickname);
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

    // 소셜 로그인 회원 생성
    public static Member createSocialMember(SocialRegisterRequest registerRequest) {
        return Member.registerWithSocial(registerRequest);
    }

    // 구글 소셜 로그인 회원
    public static Member createGoogleMember(String email, String nickname) {
        SocialRegisterRequest request = new SocialRegisterRequest(email, nickname, Provider.GOOGLE, "google_" + email);
        return Member.registerWithSocial(request);
    }

    // 네이버 소셜 로그인 회원
    public static Member createNaverMember(String email, String nickname) {
        SocialRegisterRequest request = new SocialRegisterRequest(email, nickname, Provider.NAVER, "naver_" + email);
        return Member.registerWithSocial(request);
    }

    // 카카오 소셜 로그인 회원
    public static Member createKakaoMember(String email, String nickname) {
        SocialRegisterRequest request = new SocialRegisterRequest(email, nickname, Provider.KAKAO, "kakao_" + email);
        return Member.registerWithSocial(request);
    }

    // SocialRegisterRequest 생성 헬퍼 메서드
    public static SocialRegisterRequest createSocialRegisterRequest(String email, String nickname, Provider provider, String providerId) {
        return new SocialRegisterRequest(email, nickname, provider, providerId);
    }

    // 구글용 편의 메서드
    public static SocialRegisterRequest createGoogleSocialRequest(String email, String nickname) {
        return new SocialRegisterRequest(email, nickname, Provider.GOOGLE, "google_" + email);
    }
}
