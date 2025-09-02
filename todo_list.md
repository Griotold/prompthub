# TODO List - Prompt 리팩토링 & 추가 작업

## 1. PromptLike 분리 및 리포지토리 관리
- [x] `Prompt` 엔티티에서 `List<PromptLike>` 제거
- [x] `PromptLike` 엔티티는 그대로 두고 별도 `PromptLikeRepository` 생성
- [x] 좋아요 추가/삭제, 존재 여부 확인 메서드 구현
    - `existsByPromptAndMember(Prompt, Member)`
    - `save(PromptLike)`
    - `deleteByPromptAndMember(Prompt, Member)`
    - `findByPrompt(Prompt)`

## 2. PromptModifyService 수정
- [x] `addLike` / `removeLike` 시 `PromptLikeRepository` 호출
- [x] `Prompt`에서는 `likesCount`만 관리하도록 변경
- [x] Lazy Loading 문제 제거 확인

## 3. PromptFinder / PromptRegister 설계 점검
- [x] Finder: 페이지 단위 조회, 인기순, 카테고리별, 검색
- [x] Register: 등록, 수정, 공개/비공개, 조회수 증가
- [x] Validator 호출 및 예외 처리 확인

## 4. 테스트 코드 작성 / 수정
- [ ] PromptCommandService 테스트
    - 좋아요 추가/삭제, 이미 좋아요 예외, likesCount 증가/감소
- [ ] PromptFinder 테스트
    - 공개 프롬프트, 카테고리별 조회, 인기 프롬프트, 검색
- [ ] Lazy Loading 제거 확인 테스트

## 5. 글로벌 예외 처리
- [ ] IllegalStateException, DuplicateLikeException 등 처리 여부 확인
