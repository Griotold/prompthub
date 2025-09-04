# TODO List - 소셜 로그인 구현

## ✅ 완료된 작업 (구글 소셜 로그인)

### 1. 의존성 추가 및 기본 설정
- [x] build.gradle에 Spring Security, OAuth2 Client 의존성 추가
- [x] application.yml에 OAuth2 클라이언트 설정 (구글)

### 2. Member 관련 구현
- [x] MemberRepository에 소셜 로그인용 쿼리 메서드 추가
- [x] MemberRegister 인터페이스에 소셜 로그인 메서드 추가
- [x] MemberModifyService에 소셜 로그인 로직 구현

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

---

## 🎯 오늘 할 일

### 1. 카카오 소셜 로그인 구현
- [ ] KakaoAuthService 생성
  - [ ] RestClient로 AccessToken 요청 메서드 구현
  - [ ] Kakao 사용자 정보 조회 메서드 구현
  - [ ] Member 저장/업데이트 로직 구현 (공통 MemberSaveResult 사용)
- [ ] AuthApi에 카카오 로그인 엔드포인트 추가
  - [ ] `/api/auth/kakao/login` 엔드포인트 구현
  - [ ] JwtTokenProvider를 활용하여 JWT Access/Refresh Token 발급
  - [ ] isNewMember 여부에 따라 201/200 상태 코드 분기
- [ ] application.yml에 카카오 OAuth2 설정 추가
- [ ] 테스트: 카카오 OAuth2 로그인 플로우 확인

### 2. 네이버 소셜 로그인 구현
- [ ] NaverAuthService 생성
  - [ ] RestClient로 AccessToken 요청 메서드 구현
  - [ ] Naver 사용자 정보 조회 메서드 구현
  - [ ] Member 저장/업데이트 로직 구현 (공통 MemberSaveResult 사용)
- [ ] AuthApi에 네이버 로그인 엔드포인트 추가
  - [ ] `/api/auth/naver/login` 엔드포인트 구현
  - [ ] JwtTokenProvider를 활용하여 JWT Access/Refresh Token 발급
  - [ ] isNewMember 여부에 따라 201/200 상태 코드 분기
- [ ] application.yml에 네이버 OAuth2 설정 추가
- [ ] 테스트: 네이버 OAuth2 로그인 플로우 확인

### 3. 전체 통합 및 리팩토링
- [ ] Google, Kakao, Naver 공통 코드 리팩토링
  - [ ] MemberSaveResult, TokenResponse 정적 팩토리 메서드 활용
  - [ ] RefreshTokenService와 JWT 토큰 발급 로직 점검
- [ ] AuthApi 전체 엔드포인트 점검
  - [ ] `/api/auth/google/login`
  - [ ] `/api/auth/kakao/login`
  - [ ] `/api/auth/naver/login`
  - [ ] `/api/auth/refresh` 토큰 재발급
- [ ] 통합 테스트 작성 및 검증
  - [ ] 모든 소셜 로그인 성공 시 JWT 발급 확인
  - [ ] 기존/신규 회원 상태 코드 확인
  - [ ] 리프레시 토큰으로 액세스 토큰 갱신 테스트

---

## 🔄 향후 과제

### 소셜 로그인 통합 테스트
- [ ] OAuth2 로그인 플로우 통합 테스트
- [ ] 회원 재활성화 시나리오 테스트
- [ ] Vue.js 프론트엔드 연동 테스트

### 추가 개선사항
- [ ] 에러 핸들링 및 예외 처리 강화
- [ ] 로깅 및 모니터링 개선
- [ ] API 문서화 (Swagger/OpenAPI)
