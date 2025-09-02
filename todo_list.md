# TODO List - 소셜 로그인 구현

## 1. 의존성 추가 및 기본 설정
- [x] build.gradle에 Spring Security, OAuth2 Client 의존성 추가
- [x] application.yml에 OAuth2 클라이언트 설정 (구글, 네이버, 카카오)

## 2. Member 관련 추가 구현
- [x] MemberRepository에 소셜 로그인용 쿼리 메서드 추가
- [x] MemberRegister 인터페이스에 소셜 로그인 메서드 추가
- [x] MemberModifyService에 소셜 로그인 로직 구현

## 3. Spring Security 설정
- [ ] SecurityConfig 클래스 생성
- [ ] OAuth2 로그인 성공 핸들러 구현
- [ ] 커스텀 OAuth2UserService 구현

## 4. 소셜 로그인 통합 테스트
- [ ] OAuth2 로그인 플로우 테스트
- [ ] 재활성화 시나리오 테스트