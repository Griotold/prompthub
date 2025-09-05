package com.griotold.prompthub.adapter.security.social.naver;

import com.griotold.prompthub.adapter.security.jwt.JwtTokenProvider;
import com.griotold.prompthub.adapter.security.social.MemberSaveResult;
import com.griotold.prompthub.adapter.security.social.TokenResponse;
import com.griotold.prompthub.application.member.provided.MemberFinder;
import com.griotold.prompthub.application.member.provided.MemberRegister;
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
public class NaverAuthService {

    private final RestClient restClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRegister memberRegister;
    private final MemberFinder memberFinder;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @Value("${naver.token-uri}")
    private String tokenUri;

    @Value("${naver.user-info-uri}")
    private String userInfoUri;

    public TokenResponse login(String authorizationCode) {
        log.info("네이버 로그인 시작 - 인가코드: {}", authorizationCode);

        // 1. 네이버에서 Access Token 받기
        String naverAccessToken = getNaverAccessToken(authorizationCode);

        // 2. 네이버 사용자 정보 조회
        NaverUserResponse naverUserInfo = getNaverUserInfo(naverAccessToken);

        // 3. 우리 DB에 사용자 저장/업데이트
        MemberSaveResult result = saveOrUpdateMember(naverUserInfo);

        // 4. JWT 토큰 발급
        return TokenResponse.fromMember(result.member(), result.isNewMember(), jwtTokenProvider);
    }

    private String getNaverAccessToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", authorizationCode);
        params.add("state", "random_port"); // 요청에서 받은 state 사용

        try {
            NaverTokenResponse tokenResponse = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(NaverTokenResponse.class);

            log.info("네이버 토큰 발급 성공");
            log.info("네이버 accessToken='{}'", tokenResponse.accessToken());
            return tokenResponse.accessToken();

        } catch (HttpClientErrorException.BadRequest e) {
            log.warn("네이버 토큰 발급 실패 - 잘못된 인가코드: {}", e.getMessage());
            throw new IllegalArgumentException("유효하지 않은 인가코드입니다.");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("네이버 토큰 발급 실패 - 인증 실패: {}", e.getMessage());
            throw new IllegalArgumentException("네이버 인증에 실패했습니다.");
        } catch (RestClientException e) {
            log.error("네이버 API 통신 실패: {}", e.getMessage());
            throw new RuntimeException("네이버 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    private NaverUserResponse getNaverUserInfo(String accessToken) {
        try {
            NaverUserResponse userResponse = restClient.get()
                    .uri(userInfoUri)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(NaverUserResponse.class);

            log.info("네이버 사용자 정보 조회 성공 - ID: {}", userResponse.getId());
            return userResponse;

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("네이버 사용자 정보 조회 실패 - 토큰 무효: {}", e.getMessage());
            throw new IllegalArgumentException("네이버 액세스 토큰이 유효하지 않습니다.");
        } catch (RestClientException e) {
            log.error("네이버 사용자 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("네이버 서버와 통신 중 오류가 발생했습니다.");
        }
    }

    private MemberSaveResult saveOrUpdateMember(NaverUserResponse naverUserInfo) {
        String providerId = naverUserInfo.getId();
        String email = naverUserInfo.getEmail();
        String name = naverUserInfo.getNickname();

        // 이메일이 없는 경우 처리
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("네이버 계정에서 이메일 정보를 받을 수 없습니다. 이메일 제공 동의가 필요합니다.");
        }

        Optional<Member> existingMember = memberFinder.findBySocial(Provider.NAVER, providerId);
        boolean isNewMember;
        Member savedMember;

        if (existingMember.isEmpty()) {
            // 신규 회원 생성
            SocialRegisterRequest registerRequest = new SocialRegisterRequest(email, name, Provider.NAVER, providerId);
            savedMember = memberRegister.registerWithSocial(registerRequest);
            isNewMember = true;
            log.info("신규 네이버 회원 생성: {}", email);
        } else {
            // 기존 회원 정보 업데이트 (활성화 처리)
            Member member = existingMember.get();
            member.reactivate();
            savedMember = memberRegister.save(member);
            isNewMember = false;
            log.info("기존 네이버 회원 정보 업데이트: {}", email);
        }

        return new MemberSaveResult(savedMember, isNewMember);
    }
}