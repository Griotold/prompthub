# TODO List - 소셜 로그인 구현

## ✅ 완료된 작업

### 1. 기본 설정 및 의존성
- [x] build.gradle에 Spring Security, OAuth2 Client 의존성 추가
- [x] application.yml에 OAuth2 클라이언트 설정 (구글, 카카오, 네이버)

### 2. Member 도메인 및 서비스 구현
- [x] MemberRepository에 소셜 로그인용 쿼리 메서드 추가
- [x] MemberRegister 인터페이스에 소셜 로그인 메서드 추가
- [x] MemberModifyService에 소셜 로그인 로직 구현
- [x] Provider enum에 GOOGLE, KAKAO, NAVER 추가

### 3. Spring Security 설정
- [x] SecurityConfig 클래스 생성 (CORS 설정 포함)
- [x] OAuth2 로그인 성공 핸들러 구현
- [x] 커스텀 OAuth2UserService 구현

### 4. JWT 토큰 관리
- [x] build.gradle에 jjwt 의존성 추가
- [x] JwtTokenProvider 구현
- [x] OAuth2LoginSuccessHandler에 JWT 생성 로직 추가
- [x] JwtAuthenticationFilter 구현
- [x] SecurityConfig에 JWT 필터 추가
- [x] AuthController 구현 (토큰 재발급, 검증 API)

### 5. 구글 소셜 로그인
- [x] GoogleAuthService 구현 (RestClient 기반)
- [x] GoogleTokenResponse, GoogleUserResponse DTO 구현
- [x] AuthApi에 `/api/v1/auth/google/login` 엔드포인트 추가
- [x] GoogleTestController 구현 (인가코드 테스트용)
- [x] 구글 OAuth2 user-info-uri v3 적용 (provider_id 저장 이슈 해결)
- [x] 테스트: 구글 OAuth2 로그인 플로우 정상 작동 확인

### 6. 카카오 소셜 로그인
- [x] KakaoAuthService 구현 (RestClient 기반)
- [x] KakaoTokenResponse, KakaoUserResponse DTO 구현
- [x] AuthApi에 `/api/v1/auth/kakao/login` 엔드포인트 추가
- [x] KakaoTestController 구현 (인가코드 테스트용)
- [x] 카카오 개발자 콘솔 앱 생성 및 비즈 앱 전환 완료
- [x] 테스트: 카카오 OAuth2 로그인 플로우 정상 작동 확인

### 7. 네이버 소셜 로그인 (기본 구조 구현 완료)
- [x] NaverAuthService 구현 (RestClient 기반)
- [x] NaverTokenResponse, NaverUserResponse DTO 구현
- [x] AuthApi에 `/api/v1/auth/naver/login` 엔드포인트 추가
- [x] NaverTestController 구현 (인가코드 테스트용)
- [x] 네이버 개발자센터 애플리케이션 등록 완료

---

## 🔄 향후 과제

### 네이버 소셜 로그인 활성화
- [ ] 네이버 개발자센터 검수 신청 (실서비스 런칭 시)
- [ ] 검수 승인 후 네이버 로그인 테스트 및 활성화

### 소셜 로그인 리팩토링
- [ ] Google, Kakao, Naver AuthService 공통 인터페이스 추출
- [ ] TokenResponse, UserResponse 공통 구조 통합
- [ ] 중복 코드 제거 및 추상화

### 통합 테스트
- [ ] OAuth2 로그인 플로우 통합 테스트 작성
- [ ] 회원 재활성화 시나리오 테스트
- [ ] JWT 토큰 발급 및 갱신 테스트
- [ ] Vue.js 프론트엔드 연동 테스트

### 추가 개선사항
- [ ] 에러 핸들링 및 예외 처리 강화
- [ ] 로깅 및 모니터링 개선
- [ ] API 문서화 (Swagger/OpenAPI)
- [ ] 소셜 로그인 성공률 모니터링

---

## 📋 구현 완료 현황

**활성 상태 (테스트 가능):**
- ✅ 구글 소셜 로그인
- ✅ 카카오 소셜 로그인

**구현 완료 (검수 대기 중):**
- 🟡 네이버 소셜 로그인 (네이버 개발자센터 검수 승인 필요)

**총 구현 진행률: 90% 완료**
