package com.griotold.prompthub.adapter.security.oauth2;

import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.application.member.provided.MemberRegister;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.Provider;
import com.griotold.prompthub.domain.member.SocialRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberFinder memberFinder;
    private final MemberRegister memberRegister;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 구글에서 받은 사용자 정보
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(); // "sub"

        // OAuth2 응답에서 필요한 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 기존 소셜 계정 확인
        Optional<Member> existingMember = memberFinder.findBySocial(Provider.GOOGLE, providerId);

        Member member;
        if (existingMember.isPresent()) {
            member = existingMember.get();
            // 비활성화된 계정이면 재활성화
            if (!member.isActive()) {
                member = memberRegister.reactivate(member.getId());
            }
        } else {
            // 신규 회원가입
            SocialRegisterRequest socialRequest = new SocialRegisterRequest(email, name, Provider.GOOGLE, providerId);
            member = memberRegister.registerWithSocial(socialRequest);
        }

        return new CustomOAuth2User(member, attributes, userNameAttributeName);
    }
}
