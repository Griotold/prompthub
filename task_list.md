# Task List - 백엔드 API 완성 및 마무리

## 긴급 - 0925 목
- [x] 프롬프트 삭제(비공개 처리) 리팩토링
- [ ] 좋아요 토글 분리
  - [ ] 리턴타입: void -> PromptDetailResponse

### WebAPI 엔드포인트 구현
- [ ] **Prompt API 확장 (태그 지원)**
  - [x] PromptRegisterRequest에 `List<String> tags` 필드 추가
  - [x] PromptUpdateRequest에 `List<String> tags` 필드 추가
  - [x] PromptDetailResponse에 태그 정보 포함
  - [ ] PromptDetailResponse에 리뷰 통계 내용 포함
  - [x] PromptListResponse에 태그 정보 포함
  - [ ] PromptListResponse에 리뷰 통계 내용 포함

- [x] **기존 PromptModifyService에 태그 연결 로직 추가**
  - [x] 프롬프트 등록/수정 시 태그 자동 생성/연결 구현

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