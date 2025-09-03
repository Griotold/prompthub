package com.griotold.prompthub.adapter.security.social.google;

import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.adapter.security.social.MemberSaveResult;
import com.griotold.prompthub.adapter.security.social.TokenResponse;
import com.griotold.prompthub.application.member.MemberQueryService;
import com.griotold.prompthub.application.member.required.MemberRepository;
import com.griotold.prompthub.domain.member.Member;
import com.griotold.prompthub.domain.member.Provider;
import com.griotold.prompthub.domain.member.SocialRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final RestClient restClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberQueryService memberQueryService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Value("${google.token-uri}")
    private String tokenUri;

    @Value("${google.user-info-uri}")
    private String userInfoUri;

    public TokenResponse login(String authorizationCode) {
        log.info("구글 로그인 시작 - 인가코드: {}", authorizationCode);

        // 1. 구글에서 Access Token 받기
        String googleAccessToken = getGoogleAccessToken(authorizationCode);

        // 2. 구글 사용자 정보 조회
        GoogleUserResponse googleUserInfo = getGoogleUserInfo(googleAccessToken);

        // 3. 우리 DB에 사용자 저장/업데이트
        MemberSaveResult result = saveOrUpdateMember(googleUserInfo);

        // 4. JWT 토큰 발급
        return TokenResponse.fromMember(result.member(), result.isNewMember(), jwtTokenProvider);
    }

    private String getGoogleAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);

        try {
            GoogleTokenResponse tokenResponse = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            log.info("구글 토큰 발급 성공");
            return tokenResponse.accessToken();

        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("구글 토큰 발급 실패 - 잘못된 인가코드: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 인가코드입니다.");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("구글 토큰 발급 실패 - 인증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("구글 인증에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("구글 API 통신 실패: {}", e.getMessage());
            throw new RuntimeException("구글 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    private GoogleUserResponse getGoogleUserInfo(String accessToken) {
        try {
            GoogleUserResponse userResponse = restClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleUserResponse.class);

            log.info("구글 사용자 정보 조회 성공 - ID: {}", userResponse.sub());
            return userResponse;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("구글 사용자 정보 조회 실패 - 토큰 무효: {}", e.getMessage());
            throw new IllegalArgumentException("구글 액세스 토큰이 유효하지 않습니다.");
        } catch (RestClientException e) {
            log.error("구글 사용자 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("구글 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    private MemberSaveResult saveOrUpdateMember(GoogleUserResponse googleUserInfo) {
        String providerId = googleUserInfo.sub();
        String email = googleUserInfo.email();
        String name = googleUserInfo.name();
        // String picture = googleUserInfo.picture();

        Optional<Member> existingMember = memberQueryService.findBySocial(Provider.GOOGLE, providerId);
        boolean isNewMember;
        Member savedMember;

        if (existingMember.isEmpty()) {
            // 신규 회원 생성
            SocialRegisterRequest registerRequest = new SocialRegisterRequest(email, name, Provider.GOOGLE, providerId);
            savedMember = memberRepository.save(Member.registerWithSocial(registerRequest));
            isNewMember = true;
            log.info("신규 회원 생성: {}", email);
        } else {
            // 기존 회원 정보 업데이트
            Member member = existingMember.get();
            savedMember = memberRepository.save(member);
            isNewMember = false;
            log.info("기존 회원 정보 업데이트: {}", email);
        }

        return new MemberSaveResult(savedMember, isNewMember);
    }

}
