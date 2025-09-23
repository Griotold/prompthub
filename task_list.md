# Task List - 백엔드 API 완성 및 마무리

### WebAPI 엔드포인트 구현
- [x] **Review API**
  - [x] `adapter/webapi/review/ReviewApi.java` 생성
  - [x] 프롬프트별 리뷰 조회 API (`GET /api/v1/prompts/{promptId}/reviews`)
  - [x] 리뷰 작성 API (`POST /api/v1/prompts/{promptId}/reviews`)
  - [x] 리뷰 수정 API (`PUT /api/v1/reviews/{reviewId}`)
  - [x] 리뷰 삭제 API (`DELETE /api/v1/reviews/{reviewId}`)

- [ ] **Prompt API 확장 (태그 지원)**
  - [ ] PromptRegisterRequest에 `List<String> tags` 필드 추가
  - [ ] PromptUpdateRequest에 `List<String> tags` 필드 추가
  - [ ] PromptDetailResponse에 태그 정보 포함
  - [ ] PromptListResponse에 태그 정보 포함

- [ ] **기존 PromptModifyService에 태그 연결 로직 추가**
  - [ ] 프롬프트 등록/수정 시 태그 자동 생성/연결 구현

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