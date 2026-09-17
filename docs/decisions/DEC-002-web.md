# DEC-002: 웹계층(Role B) 구현 결정 기록

| 항목 | 내용 |
|---|---|
| 날짜 | 2026-09-17 |
| 역할 | B — Controller/Service/Form/Thymeleaf |
| 범위 | `org.example.tastereview.web.*` + `templates/**` + `messages.properties`만 생성. `application.yaml`·`SecurityConfig`·엔티티·CSS·`Dockerfile`·`build.gradle`은 미수정 |
| 선행 문서 | `docs/srs/SRS-001-restaurant-review-site.md` 3~5절, `docs/adr/ADR-001/004/006`, `DESIGN.md` 6~7절 |

## 1. 패키지 구조

- `web/form`: `ReviewCreateForm`, `ReviewEditForm`, `ReviewDeleteForm`, `CommentForm`, `CommentDeleteForm`, `SearchForm`
- `web/dto`: `ReviewListItem`, `ReviewDetailView`, `StoreDetailView`, `PageWindow`
- `web/service`: `ReviewService`, `CommentService`, `StoreService`, `SearchService`
- `web/support`: `Normalize`(정규화), `DisplayTime`(시간대 포맷)
- `web/controller`: `ReviewController`, `CommentController`, `StoreController`
- `web/error`: `NotFoundException`, `WebErrorAdvice`

## 2. 선택 결정 (묻지 않고 진행)

1. **정규화 구현**: `Normalize` 유틸 단일 구현. `plain`=trim, `comparable`=trim→모든 `\s+` 제거→`Locale.ROOT` 소문자. DB 쿼리는 `LOWER`+`LIKE`로 후보를 좁히고 최종 일치 판정은 Java에서 `comparable`로 수행 (H2/PostgreSQL SQL 차이 회피).
2. **검색 동적 조건**: JPA Criteria `Specification`으로 AND 조합. 공백 제거 비교는 `cb.function("replace", ...)`(H2·PostgreSQL 공통 지원)로 공백 1칸 제거 + Java에서 파라미터의 모든 공백 제거 후 바인딩.
3. **정렬 타이브레이커**: `latest`=createdAt desc,id desc / `rating`=rating desc,createdAt desc,id desc (id는 SRS 외 안정성용 추가).
4. **페이지네이션 번호**: 현재±2개만 표시 (DESIGN 7.9의 360px 기준을 전 구간에 적용, `PageWindow` 계산).
5. **댓글 수 N+1 방지**: `CommentRepository.countByReviewIdIn` 1회 집계 쿼리로 조회 (상세 집계 방식은 아래 3절 계약 참조).
6. **상세 N+1 방지**: 리뷰+가게는 `@EntityGraph` 단건 조회 1쿼리 + 댓글 목록 1쿼리로 고정 2쿼리.
7. **시간 표시**: `DisplayTime`이 `app.display.time-zone`을 읽어 `yyyy-MM-dd HH:mm` 포맷. DTO는 표시용 문자열만 전달.
8. **삭제 구현**: `commentRepository.deleteByReviewId` 후 리뷰 삭제 (DB CASCADE 유무와 무관하게 동작).
9. **flash 키**: 성공 `flashMessage`(`role=status`), 실패 `flashError`(`role=alert`). 텍스트는 `MessageSource`로 해결 후 전달.
10. **trim 후 검증**: `@InitBinder StringTrimmerEditor(true)`로 바인딩 시점에 trim, 이후 Bean Validation 수행.
11. **flash 배치**: 성공·실패 메시지 모두 본문 상단 flash 영역에 1회 표시(`role=status`/`role=alert`).
    삭제 비밀번호 불일치 시 "삭제 폼 근처" 요구는 본문 상단 표시로 갈음한다.
12. **템플릿 CSS 클래스**(역할 C용): `site-header`, `site-header-search`, `page`, `flash-success`, `flash-error`,
    `review-list`, `review-card`, `review-card-title`, `badge`, `rating`, `review-meta`, `search-form`,
    `review-form`, `field`, `field-error`, `form-error-summary`, `form-help`, `required-mark`,
    `review-content`, `store-box`, `comments`, `comment`, `comment-delete-form`, `review-delete-form`,
    `pagination`, `pagination-current`, `empty-state`, `error-page`, `button-primary`, `button-secondary`,
    `button-danger`. 값 하드코딩·인라인 style 없음.

## 3. 역할 A에 대한 계약 (충족됨)

병렬 작업 중 A가 `config/`, `domain/`, `repository/`, `application.yaml`, `messages.properties`를 납품했고,
아래 차이만 조정했다.

- **엔티티**: 패키지·필드명 일치. 차이: `Review.rating`이 `Integer`(언박싱 사용, 문제없음).
- **리포지토리**: 가정 메서드 전부 존재. 차이: `CommentCount.getCnt()` 반환형 `Long`(수신 측 `Map<Long,Long>` 그대로 사용).
  `findByStoreIdOrderByCreatedAtDescIdDesc`에 `@EntityGraph`가 없어도 서비스 트랜잭션 내에서 조회하므로 N+1·지연로딩 문제없음.
- **예외 처리**: `NotFoundException`을 `NoSuchElementException` 상속으로 바꿔 A의 `GlobalExceptionHandler`가 404 처리.
  `WebErrorAdvice`는 `@Order(HIGHEST_PRECEDENCE)`로 `MethodArgumentTypeMismatchException`,
  `NoResourceFoundException`만 404 처리 (A의 범용 `Exception` 핸들러보다 먼저 매칭).
- **설정값**: `AppProperties`가 생겼으므로 B의 `@Value` 직접 읽기를 폐기하고 `AppProperties` 단일 원천으로 리팩터링.
- **메시지 파일**: A 작성 체계(`layout.*`, `review.form.*` 등)를 채택하고, B 필요 키 30여 개를 `messages.properties`末尾
  `# 웹계층(Role B) 추가 키` 섹션에 추가. 중복 키 버그 수정: `review.form.title`이 2회 정의(19행 `리뷰 작성`,
  39행 `제목`)되어 페이지 제목이 `제목`으로 나오던 문제를 39행 → `review.form.title.label`로 변경.
  템플릿의 제목 입력란 라벨도 `review.form.title.label`로 변경.

## 4. 검증 결과 (2026-09-17)

- `./gradlew compileJava` 통과. `./gradlew test` 46개 중 B 영역 25개(ReviewEndpointContract) 전부 통과.
- 실기동 스모크(local, H2): 작성→302 상세, 상세 title `제목 - 가게 | 사이트`, 댓글 PRG `#comments`,
  수정 불일치 200+메시지·성공 302, 삭제 불일치 302 상세·성공 302 목록+flash, 공백변형 가게 재사용+평균 4.0,
  리뷰 전삭제 후 `아직 리뷰가 없습니다`, 숫자아닌 ID·미매핑 경로 404, 무효 검색값 무시 200.
- 수정한 버그 3건: (1) `th:each`+`th:replace` 동시 사용 시 fragment 파라미터 null → `th:block` 감싸기
  (list.html, stores/detail.html). (2) `ReviewService.findNormalizedStore`가 구 조회 메서드 참조 → A의
  `searchByNormalizedName`으로 교체. (3) 위 중복 키.
- 남은 실패 2건은 범위 밖: `REQ-NF-006`(CSRF 403 본문 없음 — 필터단 거부라 A의 `@ExceptionHandler`가 못 받음,
  A의 `SecurityConfig`에서 accessDeniedHandler 지정 필요), `DeployFilesContractTest`(C의 Dockerfile).
- `utext`·인라인 style·템플릿 한글 리터럴 없음 (grep 확인). `th:text`만 사용.
