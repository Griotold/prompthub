# Task List - 백엔드 API 완성 및 마무리

## 0912 금

### 긴급
- [x] 테스트에서 어노테이션을 묶어서 표준 어노테이션을 만들어버리자
- [x] 멘토님으로부터 받은 피드백 검토 후 DDL에 추가할 거 추가하기
- [x] Prompt 에 가격 관련 필드 추가
- [ ] PromptPurchase 엔티티 추가
### Tag 도메인 구현
- [ ] **Tag provided 인터페이스 정의**
  - [ ] `application/tag/provided/TagRegister.java` 인터페이스 생성
  - [ ] `application/tag/provided/TagFinder.java` 인터페이스 생성
- [ ] **Tag 서비스 구현체**
  - [ ] `application/tag/TagModifyService.java` 생성 (TagRegister 구현)
  - [ ] `application/tag/TagQueryService.java` 생성 (TagFinder 구현)

### Review 도메인 구현
- [ ] **Review provided 인터페이스 정의**
  - [ ] `application/review/provided/ReviewRegister.java` 인터페이스 생성
  - [ ] `application/review/provided/ReviewFinder.java` 인터페이스 생성
- [ ] **Review 서비스 구현체**
  - [ ] `application/review/ReviewModifyService.java` 생성 (ReviewRegister 구현)
  - [ ] `application/review/ReviewQueryService.java` 생성 (ReviewFinder 구현)

### PromptTag 연결 로직 추가
- [ ] **PromptTag provided 인터페이스 정의**
  - [ ] `application/prompt/provided/PromptTagRegister.java` 인터페이스 생성
  - [ ] `application/prompt/provided/PromptTagFinder.java` 인터페이스 생성
- [ ] **기존 PromptModifyService 확장**
  - [ ] 프롬프트 등록/수정 시 태그 자동 생성/연결 로직 추가

### WebAPI 엔드포인트 구현
- [ ] **Tag 컨트롤러**
  - [ ] `adapter/webapi/tag/TagController.java` 생성
  - [ ] 태그 목록 조회 API 구현
  - [ ] 태그 검색 API 구현
- [ ] **Review 컨트롤러**
  - [ ] `adapter/webapi/review/ReviewController.java` 생성
  - [ ] 리뷰 등록 API 구현
  - [ ] 리뷰 수정 API 구현
  - [ ] 리뷰 삭제 API 구현
  - [ ] 프롬프트별 리뷰 조회 API 구현 (인프런 스타일)
  - [ ] 더보기 API 구현

### DTO 및 응답 객체 구현
- [ ] **Tag 관련 DTO**
  - [ ] `adapter/webapi/dto/tag/` 패키지 생성
  - [ ] TagResponse 구현
  - [ ] TagListResponse 구현
- [ ] **Review 관련 DTO**
  - [ ] `adapter/webapi/dto/review/` 패키지 생성
  - [ ] ReviewResponse 구현
  - [ ] ReviewListResponse 구현
  - [ ] PromptReviewResponse 구현 (내 리뷰 + 다른 리뷰들)

### WebAPI 엔드포인트 구현
- [ ] **Tag 컨트롤러**
  - `adapter/webapi/tag/TagController.java` 생성
  - 태그 목록 조회, 검색 API 구현

- [ ] **Review 컨트롤러**
  - `adapter/webapi/review/ReviewController.java` 생성
  - 리뷰 CRUD, 프롬프트별 리뷰 조회 API 구현
  - 인프런 스타일 더보기 API 구현

- [ ] **PromptTag 연결 로직**
  - 기존 PromptModifyService에 태그 연결 로직 추가
  - 프롬프트 등록/수정 시 태그 자동 생성/연결 구현

### DTO 및 응답 객체 구현
- [ ] **Tag 관련 DTO**
  - `adapter/webapi/dto/tag/` 패키지 생성
  - TagResponse, TagListResponse 등 구현

- [ ] **Review 관련 DTO**
  - `adapter/webapi/dto/review/` 패키지 생성
  - ReviewResponse, ReviewListResponse, PromptReviewResponse 등 구현

## 이번 주 (백엔드 MVP 완성)
- [ ] Gemini 를 리뷰 도구로 등록하기
- [ ] 테스트 CI 도입
- [ ] 태그 시스템 구현
  - [ ] 프롬프트별 태그 추가/관리 API
  - [ ] 태그 기반 검색/필터링
- [ ] 평점 + 리뷰 시스템 구현
  - [ ] 별점(1-5) + 텍스트 리뷰 작성 API
  - [ ] 평점 분포 차트 데이터 제공
  - [ ] 평균 평점 계산 로직
- [ ] 기본 통계 기능
  - [ ] 조회수 카운트
  - [ ] 좋아요 기능
- [ ] 소셜 로그인할 때 프로필 이미지도 같이 받아오는지 체크
  - [ ] 구글
  - [ ] 카카오
  - [ ] 네이버

## Nice To Have
- [ ] 관련 프롬프트 추천 알고리즘
  - [ ] 카테고리/태그 기반 추천
  - [ ] 사용자 행동 기반 추천
- [ ] 팔로우 시스템
  - [ ] 사용자 팔로우/언팔로우
  - [ ] 팔로잉 사용자 프롬프트 피드
- [ ] 저장(북마크) 기능

## MVP 이후 고려사항
- [ ] 댓글 시스템 (리뷰로 충분할 수 있음)
- [ ] 프롬프트 공유 기능
- [ ] 다크모드 토글
- [ ] 알림 시스템
- [ ] 프롬프트 히스토리/버전 관리