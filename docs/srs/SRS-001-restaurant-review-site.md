# SRS-001: 맛집 리뷰 사이트

| 항목 | 내용 |
|---|---|
| 상태 | Approved |
| 작성자 | a1l1ke |
| 작성일 | 2026-09-17 |
| 최종 수정일 | 2026-09-17 |
| 원본 PRD | [PRD-001](../prd/PRD-001-restaurant-review-site.md) |

## 1. 소개

### 1.1 목적

PRD-001(맛집 리뷰 사이트)을 AI 코딩 도구로 1시간 안에 구현하고 테스트할 수 있도록, 요구사항을 검증 가능한 단위로 정의한다.
독자는 구현을 맡은 개발자(AI 에이전트 포함)와 리뷰어다.

### 1.2 범위

- 포함: 익명 리뷰 작성·조회·수정·삭제, 가게 정보, 키워드·필터 검색, 익명 댓글, SSR 화면, Docker 이미지, Render 배포, Neon PostgreSQL 연결.
- 제외: PRD-001의 제외 범위(로그인, 사진, 태그·가격대, 지도, 대댓글·좋아요·신고·관리자, REST API·SPA)와 요청 횟수 제한, 비밀번호 실패 잠금.

### 1.3 용어 정의

| 용어 | 정의 |
|---|---|
| SSR | 서버가 Thymeleaf로 완성된 HTML을 만들어 응답하는 방식 |
| PRG | POST 처리 후 302로 GET 페이지에 리다이렉트하는 패턴 |
| 작성 비밀번호 | 리뷰·댓글을 쓸 때 입력하고, 수정·삭제 때 대조하는 비밀번호 |
| 정규화 | 문자열 앞뒤 공백을 제거하는 처리. 비교용 정규화는 모든 공백 제거와 소문자 변환까지 포함한다 |
| 프로필 | Spring 프로필 `local`, `test`, `prod` |
| Render | 컨테이너를 실행하는 배포 플랫폼 (Docker 웹 서비스) |
| Neon | 서버리스 PostgreSQL 서비스 |

## 2. 전체 설명

### 2.1 시스템 개요

```
브라우저 ──HTTPS──> Render (TLS 종료, 프록시)
                     └─HTTP─> Docker 컨테이너 (Spring Boot, $PORT)
                                  └─JDBC(SSL)─> Neon PostgreSQL
```

- 로컬 개발과 테스트에서는 컨테이너와 Neon 대신 H2 인메모리 DB를 쓴다.

### 2.2 사용자 유형

| 사용자 | 설명 | 할 수 있는 일 |
|---|---|---|
| 방문자 | 로그인하지 않은 모든 사용자 | 조회, 검색, 리뷰·댓글 작성 |
| 작성자 | 작성 비밀번호를 아는 방문자 | 자신이 쓴 리뷰 수정·삭제, 댓글 삭제 |

### 2.3 운영 환경

| 프로필 | 용도 | DB | 실행 방법 |
|---|---|---|---|
| `local` | 로컬 개발 | H2 인메모리 (PostgreSQL 호환 모드) | `./gradlew bootRun` |
| `test` | 자동 테스트 | H2 인메모리 (PostgreSQL 호환 모드) | `./gradlew test` |
| `prod` | Render 배포 | Neon PostgreSQL | Docker 컨테이너 |

- 지원 브라우저: 최신 Chrome, Safari, Edge, Firefox와 너비 360px 이상의 모바일 브라우저.

### 2.4 제약 사항

- **기술 스택**: Java 17, Spring Boot 4.1.1, Gradle(wrapper 9.7.1), Spring MVC, Thymeleaf, Lombok. 추가 모듈은 부록 A를 따른다.
- **구현 시간**: AI 코딩 도구로 1시간 안에 구현을 마친다. 그래서 모든 화면은 JavaScript 없이 동작하는 HTML 폼으로 만들고, CSS는 최소한으로 쓴다.
- **설정 외부화**: 환경마다 달라지거나 운영 중 바꿀 수 있는 값은 코드에 쓰지 않고 설정 파일과 환경 변수로 주입한다 (REQ-NF-010~013).
- **배포**: Render의 Docker 웹 서비스로 배포한다. Render는 `PORT` 환경 변수로 포트를 알려 주고, HTTPS는 Render 프록시가 처리한다.
- **DB**: 운영은 Neon PostgreSQL, 로컬·테스트는 H2다. 스키마는 두 DB에서 모두 실행되는 SQL로 작성한다.

### 2.5 가정 및 의존성

- Render와 Neon 계정, Neon 데이터베이스와 접속 정보가 준비되어 있다고 가정한다.
- Render 무료 플랜처럼 메모리가 512MB인 환경에서도 실행된다고 가정한다.
- Render 무료 플랜은 일정 시간 요청이 없으면 서비스를 멈추고, Neon도 유휴 시 컴퓨트를 멈춘다. 이때 첫 요청은 성능 기준에서 제외한다.
- PRD에 없는 세부 결정은 이 문서에서 기본값으로 정했으며, 8절 "임의로 정한 값"에 모아 두었다.

## 3. 기능 요구사항

공통 규칙
- 모든 텍스트 입력은 저장 전에 앞뒤 공백을 제거한 뒤 검증한다.
- 검증에 실패하면 HTTP 200으로 같은 폼을 다시 보여 주고, 입력값(비밀번호 제외)을 유지하며, 항목 옆에 오류 메시지를 표시해야 한다.
- 모든 사용자 노출 문구는 메시지 파일(`messages.properties`)에서 가져와야 한다.
- 쓰기 요청이 성공하면 PRG 패턴으로 리다이렉트해야 한다.

### 3.1 리뷰 작성 (출처: FR-1, FR-2)

#### REQ-FUNC-001: 리뷰 작성 폼 표시

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /reviews/new` 요청에 가게 찾기 폼, 새 가게 입력 폼, 리뷰 입력 폼을 한 페이지로 보여 줘야 한다. |
| 출처 | FR-1, FR-2 |
| 우선순위 | Must |
| 검증 방법 | Test |

- 카테고리 선택지는 설정값 `app.store.categories`(REQ-NF-011)의 순서대로 표시해야 한다.
- 지역 입력란에는 메시지 파일에 정의한 예시 문구("서울 종로구")를 placeholder로 표시해야 한다.
- 작성 비밀번호를 잊으면 수정·삭제할 수 없다는 안내 문구를 표시해야 한다.

#### REQ-FUNC-002: 기존 가게 찾기

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /reviews/new?storeQuery={검색어}` 요청에 가게 이름에 검색어가 포함된 가게 목록을 보여 줘야 한다. |
| 출처 | FR-2 |
| 우선순위 | Must |
| 검증 방법 | Test |

**처리 흐름**
1. 검색어의 비교용 정규화 값이 가게 이름의 비교용 정규화 값에 포함되는 가게를 찾는다.
2. 결과를 가게 이름 오름차순으로 최대 `app.store.search-limit`(기본 10)개 보여 준다.
3. 각 가게에는 이름, 주소, 지역, 카테고리와 "이 가게 선택" 링크(`/reviews/new?storeId={id}`)를 표시한다.

**예외 흐름**
- 검색어가 비었거나 공백뿐이면 → 가게 목록 없이 폼만 보여 준다.
- 결과가 없으면 → "일치하는 가게가 없습니다. 새 가게 정보를 입력하세요" 안내를 보여 준다.

#### REQ-FUNC-003: 기존 가게 선택

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /reviews/new?storeId={id}` 요청에 선택한 가게 정보를 읽기 전용으로 표시하고, 새 가게 입력 폼을 숨겨야 한다. |
| 출처 | FR-2 |
| 우선순위 | Must |
| 검증 방법 | Test |

**예외 흐름**
- 존재하지 않는 `storeId`이면 → 가게를 선택하지 않은 상태의 폼과 "선택한 가게를 찾을 수 없습니다" 메시지를 보여 준다.

#### REQ-FUNC-004: 리뷰 등록

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `POST /reviews` 요청의 입력이 모두 유효하면 리뷰를 저장하고 `/reviews/{id}`로 302 리다이렉트해야 한다. |
| 출처 | FR-1, FR-2 |
| 우선순위 | Must |
| 검증 방법 | Test |

**입력**
| 항목 | 타입 | 필수 | 제약 | 오류 시 동작 |
|---|---|---|---|---|
| `storeId` | 정수 | 조건부 | 존재하는 가게 ID. 있으면 가게 입력 항목은 무시 | 폼 재표시, "선택한 가게를 찾을 수 없습니다" |
| `storeName` | 문자열 | `storeId`가 없을 때 | 1~50자 | 폼 재표시, 항목 오류 |
| `storeAddress` | 문자열 | `storeId`가 없을 때 | 1~200자 | 폼 재표시, 항목 오류 |
| `storeRegion` | 문자열 | `storeId`가 없을 때 | 1~30자 | 폼 재표시, 항목 오류 |
| `storeCategory` | 문자열 | `storeId`가 없을 때 | `app.store.categories` 중 하나 | 폼 재표시, 항목 오류 |
| `nickname` | 문자열 | 예 | 2~20자 | 폼 재표시, 항목 오류 |
| `password` | 문자열 | 예 | 4~20자 | 폼 재표시, 항목 오류, 값은 비움 |
| `rating` | 정수 | 예 | 1~5 | 폼 재표시, 항목 오류 |
| `title` | 문자열 | 예 | 1~100자 | 폼 재표시, 항목 오류 |
| `content` | 문자열 | 예 | 10~5,000자 | 폼 재표시, 항목 오류 |

**처리 흐름**
1. 입력을 정규화하고 검증한다.
2. `storeId`가 있으면 그 가게를 쓴다.
3. `storeId`가 없으면, 정규화한 이름과 주소가 모두 같은 가게가 있는지 찾는다. 있으면 그 가게를 쓰고(입력한 지역·카테고리는 무시), 없으면 새 가게를 저장한다.
4. 작성 비밀번호를 BCrypt로 해시해 리뷰와 함께 저장한다.
5. `/reviews/{새 리뷰 id}`로 리다이렉트한다.

**예외 흐름**
- 검증 실패 → 리뷰와 가게를 저장하지 않고 폼을 다시 보여 준다.
- CSRF 토큰이 없거나 틀림 → 403을 응답하고 저장하지 않는다.
- 동시에 같은 가게가 새로 등록되어 유니크 제약에 걸림 → 이미 저장된 가게를 다시 조회해 리뷰를 연결한다.

**출력**
- 302 → `/reviews/{id}`. 리다이렉트 후 새로고침해도 리뷰가 다시 등록되지 않는다.

### 3.2 리뷰 조회 (출처: FR-3)

#### REQ-FUNC-005: 리뷰 목록

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /`과 `GET /reviews` 요청에 리뷰를 최신순으로 한 페이지에 `app.pagination.page-size`(기본 20)개씩 보여 줘야 한다. |
| 출처 | FR-3 |
| 우선순위 | Must |
| 검증 방법 | Test |

- 각 항목에는 제목(상세 링크), 가게 이름, 별점(숫자 텍스트 포함), 닉네임, 작성일, 댓글 수를 표시해야 한다.
- 페이지 이동 링크(이전, 다음, 페이지 번호)를 표시하고, 링크에는 현재 검색 조건을 그대로 유지해야 한다.
- 최신순은 작성 시각 내림차순, 같으면 ID 내림차순이다.
- `GET /`은 `GET /reviews`와 같은 화면을 보여 준다.

#### REQ-FUNC-006: 리뷰 상세

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /reviews/{id}` 요청에 리뷰 전체, 가게 정보(가게 상세 링크 포함), 댓글 목록, 댓글 작성 폼, 리뷰 수정 링크, 리뷰 삭제 폼을 보여 줘야 한다. |
| 출처 | FR-3, FR-4, FR-7 |
| 우선순위 | Must |
| 검증 방법 | Test |

- 본문의 줄바꿈은 화면에서도 줄바꿈으로 보여야 한다.
- 수정된 리뷰는 "수정됨" 표시와 수정 시각을 보여야 한다.
- 모든 내용은 JavaScript 없이 HTML에 포함되어야 한다.

**예외 흐름**
- 존재하지 않는 ID, 숫자가 아닌 ID → 404 오류 페이지.

### 3.3 리뷰 수정·삭제 (출처: FR-4)

#### REQ-FUNC-007: 리뷰 수정

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `POST /reviews/{id}/edit` 요청의 작성 비밀번호가 일치하고 입력이 유효하면 리뷰를 수정하고 `/reviews/{id}`로 리다이렉트해야 한다. |
| 출처 | FR-4 |
| 우선순위 | Must |
| 검증 방법 | Test |

- `GET /reviews/{id}/edit`은 현재 제목, 본문, 별점이 채워진 수정 폼과 비밀번호 입력란을 보여 준다.
- 수정할 수 있는 항목은 제목, 본문, 별점이다. 가게, 닉네임, 작성 비밀번호는 바꿀 수 없다.
- 입력 제약은 REQ-FUNC-004의 같은 항목과 같다.
- 수정에 성공하면 수정 시각을 기록한다.

**예외 흐름**
- 비밀번호 불일치 → 수정하지 않고 수정 폼을 다시 보여 주며 "비밀번호가 일치하지 않습니다"를 표시한다. 입력한 제목·본문·별점은 유지한다.
- 검증 실패 → 수정하지 않고 항목 오류와 함께 수정 폼을 다시 보여 준다.
- 없는 리뷰 → 404.
- 실패 횟수와 관계없이 잠금 없이 매번 대조한다.

#### REQ-FUNC-008: 리뷰 삭제

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `POST /reviews/{id}/delete` 요청의 작성 비밀번호가 일치하면 리뷰와 그 리뷰의 모든 댓글을 DB에서 영구 삭제하고 `/reviews`로 리다이렉트해야 한다. |
| 출처 | FR-4 |
| 우선순위 | Must |
| 검증 방법 | Test |

- 삭제 후 목록, 검색, 가게 상세, 평균 별점 어디에도 삭제된 리뷰의 흔적이 남지 않아야 한다.
- 리뷰가 하나도 남지 않은 가게는 삭제하지 않는다.
- 성공하면 목록 화면에 "리뷰가 삭제되었습니다" 안내를 1회 보여 준다 (flash 속성).

**예외 흐름**
- 비밀번호 불일치 → 삭제하지 않고 `/reviews/{id}`로 리다이렉트하며, 상세 화면의 삭제 폼 근처에 "비밀번호가 일치하지 않습니다"를 1회 보여 준다.
- 없는 리뷰 → 404.

### 3.4 가게 상세 (출처: FR-5)

#### REQ-FUNC-009: 가게 상세

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /stores/{id}` 요청에 가게 이름, 주소, 지역, 카테고리, 평균 별점, 리뷰 수, 그 가게의 리뷰 목록을 보여 줘야 한다. |
| 출처 | FR-5 |
| 우선순위 | Should |
| 검증 방법 | Test |

- 평균 별점은 소수점 둘째 자리에서 반올림(HALF_UP)해 첫째 자리까지 표시한다. 예: 4, 5, 5 → 4.7.
- 리뷰가 없으면 평균 별점 대신 "아직 리뷰가 없습니다"를 표시한다.
- 리뷰 목록은 REQ-FUNC-005와 같은 형식, 정렬, 페이지 크기(`page` 파라미터)를 쓴다.

**예외 흐름**
- 없는 가게 → 404.

### 3.5 검색 (출처: FR-6)

#### REQ-FUNC-010: 리뷰 검색

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `GET /reviews`의 검색 파라미터를 모두 AND로 조합해 조건에 맞는 리뷰만 보여 줘야 한다. |
| 출처 | FR-6 |
| 우선순위 | Must |
| 검증 방법 | Test |

**입력 (쿼리 파라미터)**
| 항목 | 타입 | 필수 | 제약 | 값이 비었거나 잘못됐을 때 |
|---|---|---|---|---|
| `keyword` | 문자열 | 아니오 | 최대 100자 | 비었으면 조건 없음. 100자 초과분은 잘라서 사용 |
| `region` | 문자열 | 아니오 | 최대 30자 | 비었으면 조건 없음. 30자 초과분은 잘라서 사용 |
| `category` | 문자열 | 아니오 | `app.store.categories` 중 하나 | 목록에 없으면 조건 없음 |
| `minRating` | 정수 | 아니오 | 1~5 | 범위 밖이거나 숫자가 아니면 조건 없음 |
| `sort` | 문자열 | 아니오 | `latest`, `rating` | 그 밖의 값이면 `latest` |
| `page` | 정수 | 아니오 | 1 이상 (1부터 시작) | 1 미만이거나 숫자가 아니면 1 |

**처리 흐름**
1. `keyword`: 비교용 정규화한 검색어가 가게 이름, 리뷰 제목, 리뷰 본문 중 하나라도 비교용 정규화 값에 포함되면 일치한다.
2. `region`: 비교용 정규화한 값이 가게 지역의 비교용 정규화 값에 포함되면 일치한다. 예: "종로"는 "서울 종로구"와 일치한다.
3. `category`: 가게 카테고리와 정확히 같으면 일치한다.
4. `minRating`: 리뷰 별점이 그 값 이상이면 일치한다.
5. 정렬: `latest`는 REQ-FUNC-005와 같고, `rating`은 별점 내림차순, 같으면 작성 시각 내림차순이다.
6. 검색 폼은 현재 조건 값을 채운 상태로 다시 표시한다.

**예외 흐름**
- 결과가 없으면 → "검색 결과가 없습니다" 안내와 조건 초기화 링크(`/reviews`)를 보여 준다.
- `page`가 마지막 페이지보다 크면 → 결과 없음 안내를 보여 준다.

**출력**
- 같은 URL로 다시 요청하면 같은 조건과 결과가 표시된다.

### 3.6 댓글 (출처: FR-7)

#### REQ-FUNC-011: 댓글 작성

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `POST /reviews/{id}/comments` 요청의 입력이 유효하면 댓글을 저장하고 `/reviews/{id}#comments`로 리다이렉트해야 한다. |
| 출처 | FR-7 |
| 우선순위 | Must |
| 검증 방법 | Test |

**입력**
| 항목 | 타입 | 필수 | 제약 | 오류 시 동작 |
|---|---|---|---|---|
| `nickname` | 문자열 | 예 | 2~20자 | 리뷰 상세를 HTTP 200으로 다시 표시, 댓글 폼에 항목 오류 |
| `password` | 문자열 | 예 | 4~20자 | 위와 같음, 값은 비움 |
| `content` | 문자열 | 예 | 1~500자 | 위와 같음 |

- 댓글 목록은 작성 시각 오름차순(같으면 ID 오름차순)으로 모두 표시한다. 대댓글과 수정 기능은 없다.

**예외 흐름**
- 없는 리뷰 → 404.

#### REQ-FUNC-012: 댓글 삭제

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 `POST /comments/{id}/delete` 요청의 작성 비밀번호가 일치하면 댓글을 DB에서 영구 삭제하고, 그 댓글이 속했던 리뷰의 `/reviews/{reviewId}#comments`로 리다이렉트해야 한다. |
| 출처 | FR-7 |
| 우선순위 | Must |
| 검증 방법 | Test |

- 댓글마다 비밀번호 입력란과 삭제 버튼이 있는 폼을 둔다.

**예외 흐름**
- 비밀번호 불일치 → 삭제하지 않고 리뷰 상세로 리다이렉트하며 "비밀번호가 일치하지 않습니다"를 1회 보여 준다.
- 없는 댓글 → 404.

### 3.7 공통 화면 (출처: PRD 7절)

#### REQ-FUNC-013: 오류 페이지

| 항목 | 내용 |
|---|---|
| 설명 | 시스템은 404, 403, 500 오류에 사이트 레이아웃을 갖춘 오류 페이지를 보여 줘야 하며, 스택 트레이스나 예외 메시지를 노출하지 않아야 한다. |
| 출처 | PRD 7절 (보안) |
| 우선순위 | Must |
| 검증 방법 | Test |

## 4. 외부 인터페이스 요구사항

### 4.1 사용자 인터페이스

#### REQ-IF-001: 공통 레이아웃

- 모든 페이지는 사이트 이름(`app.site.name`), 홈 링크, 리뷰 작성 링크, 검색 폼을 담은 머리글을 가져야 한다.
- 모든 페이지는 페이지마다 다른 `<title>`과 `<meta name="description">`을 가져야 한다. 리뷰 상세의 제목은 "리뷰 제목 - 가게 이름 | 사이트 이름" 형식이다.
- 모든 입력 필드는 `<label>`과 연결되어야 한다.
- 너비 360px 화면에서 가로 스크롤이 생기지 않아야 한다.
- JavaScript 없이 모든 기능을 쓸 수 있어야 한다.

#### REQ-IF-002: 화면 목록

| 화면 | 템플릿 기준 URL | 관련 요구사항 |
|---|---|---|
| 리뷰 목록·검색 결과 | `/`, `/reviews` | REQ-FUNC-005, 010 |
| 리뷰 작성 | `/reviews/new` | REQ-FUNC-001~004 |
| 리뷰 상세 | `/reviews/{id}` | REQ-FUNC-006, 008, 011, 012 |
| 리뷰 수정 | `/reviews/{id}/edit` | REQ-FUNC-007 |
| 가게 상세 | `/stores/{id}` | REQ-FUNC-009 |
| 오류 | - | REQ-FUNC-013 |

### 4.2 HTTP 엔드포인트

| ID | 메서드 | 경로 | 파라미터 | 성공 응답 | 실패 응답 |
|---|---|---|---|---|---|
| REQ-IF-010 | GET | `/` | REQ-FUNC-010과 같음 | 200, 리뷰 목록 | - |
| REQ-IF-011 | GET | `/reviews` | `keyword`, `region`, `category`, `minRating`, `sort`, `page` | 200, 리뷰 목록 | - |
| REQ-IF-012 | GET | `/reviews/new` | `storeQuery`, `storeId` (선택) | 200, 작성 폼 | - |
| REQ-IF-013 | POST | `/reviews` | REQ-FUNC-004 입력 | 302 → `/reviews/{id}` | 200 폼 재표시, 403(CSRF) |
| REQ-IF-014 | GET | `/reviews/{id}` | - | 200, 상세 | 404 |
| REQ-IF-015 | GET | `/reviews/{id}/edit` | - | 200, 수정 폼 | 404 |
| REQ-IF-016 | POST | `/reviews/{id}/edit` | `password`, `title`, `content`, `rating` | 302 → `/reviews/{id}` | 200 폼 재표시, 404, 403(CSRF) |
| REQ-IF-017 | POST | `/reviews/{id}/delete` | `password` | 302 → `/reviews` | 302 → `/reviews/{id}`(불일치), 404, 403(CSRF) |
| REQ-IF-018 | POST | `/reviews/{id}/comments` | `nickname`, `password`, `content` | 302 → `/reviews/{id}#comments` | 200 상세 재표시, 404, 403(CSRF) |
| REQ-IF-019 | POST | `/comments/{id}/delete` | `password` | 302 → `/reviews/{reviewId}#comments` | 302(불일치), 404, 403(CSRF) |
| REQ-IF-020 | GET | `/stores/{id}` | `page` | 200, 가게 상세 | 404 |
| REQ-IF-021 | GET | `/actuator/health` | - | 200, `{"status":"UP"}` | 503 (DB 연결 실패) |

- 위 목록에 없는 경로는 404를 응답해야 한다.
- 모든 쓰기 요청은 POST이며 CSRF 토큰이 필요하다.
- Actuator는 `health` 엔드포인트만 외부에 노출하고, 세부 정보(`show-details`)는 노출하지 않아야 한다.

### 4.3 외부 시스템 인터페이스

#### REQ-IF-030: Neon PostgreSQL 연결

- `prod` 프로필은 환경 변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 DB에 연결해야 한다.
- `DB_URL`은 JDBC 형식(`jdbc:postgresql://<host>/<db>?sslmode=require`)이어야 한다. Neon 콘솔이 주는 `postgresql://user:pass@host/db` 형식을 그대로 넣으면 연결되지 않으므로, 배포 문서(REQ-NF-024)에 변환 방법을 적어야 한다.
- SSL 없이 연결하지 않아야 한다 (`sslmode=require`).
- Neon의 pooled 연결 주소(`-pooler`가 붙은 호스트)를 사용할 수 있어야 한다.
- 커넥션 풀 최대 크기는 `DB_POOL_MAX_SIZE`(기본 5)로 조정할 수 있어야 한다.

#### REQ-IF-031: Render 연결

- 애플리케이션은 환경 변수 `PORT`의 포트에서 요청을 받아야 하며, `PORT`가 없으면 8080을 써야 한다.
- Render 프록시가 보내는 `X-Forwarded-*` 헤더를 반영해, 리다이렉트 URL이 `https`로 만들어져야 한다.
- Render 헬스 체크 경로는 `/actuator/health`로 설정한다.

## 5. 데이터 요구사항

공통
- 모든 시각은 UTC 기준 시점(timestamp with time zone 또는 동등한 값)으로 저장하고, 화면에는 `app.display.time-zone`(기본 `Asia/Seoul`) 기준 `yyyy-MM-dd HH:mm` 형식으로 보여 준다.
- 기본 키는 DB가 생성하는 64비트 정수다.
- 스키마는 Flyway 마이그레이션으로만 만들고 바꾼다 (REQ-DATA-004).

### REQ-DATA-001: 가게 (`store`)

| 속성 | 타입 | 필수 | 제약 | 설명 |
|---|---|---|---|---|
| `id` | bigint | 예 | PK | |
| `name` | varchar(50) | 예 | | 가게 이름 |
| `address` | varchar(200) | 예 | | 주소 |
| `region` | varchar(30) | 예 | | 사용자가 입력한 지역 |
| `category` | varchar(30) | 예 | | 설정 목록의 값을 문자열로 저장 |
| `created_at` | timestamp | 예 | | 등록 시각 |

- (`name`, `address`)에 유니크 제약을 둔다.
- 카테고리는 DB enum이나 코드 enum이 아닌 문자열로 저장해, 설정만 바꿔 목록을 늘릴 수 있게 한다.
- 가게는 삭제하지 않는다.

### REQ-DATA-002: 리뷰 (`review`)

| 속성 | 타입 | 필수 | 제약 | 설명 |
|---|---|---|---|---|
| `id` | bigint | 예 | PK | |
| `store_id` | bigint | 예 | FK → `store.id` | |
| `nickname` | varchar(20) | 예 | | |
| `password_hash` | varchar(100) | 예 | | BCrypt 해시 |
| `title` | varchar(100) | 예 | | |
| `content` | varchar(5000) | 예 | | |
| `rating` | smallint | 예 | 1~5 (CHECK 제약) | |
| `created_at` | timestamp | 예 | | 작성 시각 |
| `updated_at` | timestamp | 아니오 | | 마지막 수정 시각. 수정한 적 없으면 NULL |

- 인덱스: `store_id`, `created_at`.
- 삭제 방식: 하드 삭제. 삭제 여부 컬럼을 두지 않는다.

### REQ-DATA-003: 댓글 (`comment`)

| 속성 | 타입 | 필수 | 제약 | 설명 |
|---|---|---|---|---|
| `id` | bigint | 예 | PK | |
| `review_id` | bigint | 예 | FK → `review.id`, 리뷰 삭제 시 함께 삭제 (`ON DELETE CASCADE`) | |
| `nickname` | varchar(20) | 예 | | |
| `password_hash` | varchar(100) | 예 | | BCrypt 해시 |
| `content` | varchar(500) | 예 | | |
| `created_at` | timestamp | 예 | | 작성 시각 |

- 인덱스: `review_id`.
- 삭제 방식: 하드 삭제.

### REQ-DATA-004: 스키마 마이그레이션

- 공통 마이그레이션은 H2(PostgreSQL 호환 모드)와 PostgreSQL에서 모두 실행되는 SQL로 작성해야 한다.
- DB 전용 마이그레이션은 `spring.flyway.locations`의 `{vendor}` 자리표시자로 나눈 경로(예: `db/migration/postgresql`)에 두어야 한다.
- `prod`에서는 JPA의 스키마 자동 생성을 끄고(`ddl-auto=validate`) 마이그레이션 결과만 사용해야 한다.
- PostgreSQL 전용 검색 인덱스(`pg_trgm`)는 Could 요구사항이다 (REQ-NF-002).

## 6. 비기능 요구사항

### 6.1 성능

| ID | 분류 | 요구사항 | 측정 기준 | 출처 | 검증 방법 |
|---|---|---|---|---|---|
| REQ-NF-001 | 성능 | 목록·검색·상세 페이지는 리뷰 10만 건, 댓글 50만 건에서 서버 응답 시간을 만족해야 한다. | p95 500ms 이내 (콜드 스타트 제외) | PRD 2절 | Analysis (MVP 이후 부하 측정) |
| REQ-NF-002 | 성능 | `prod` DB는 가게 이름, 리뷰 제목·본문, 지역의 부분 일치 검색에 `pg_trgm` 인덱스를 써야 한다. | 마이그레이션 존재 | PRD 9절 | Inspection (Could) |
| REQ-NF-003 | 성능 | 목록 한 페이지를 만들 때 리뷰 수에 비례해 쿼리가 늘어나지 않아야 한다 (N+1 금지). 댓글 수는 한 번의 집계 쿼리로 가져온다. | 페이지당 쿼리 수가 리뷰 수와 무관 | PRD 7절 | Inspection |
| REQ-NF-004 | 성능 | 뷰 렌더링 중 지연 로딩을 하지 않도록 `spring.jpa.open-in-view`를 꺼야 한다. | 설정값 `false` | - | Inspection |

### 6.2 보안

| ID | 분류 | 요구사항 | 측정 기준 | 출처 | 검증 방법 |
|---|---|---|---|---|---|
| REQ-NF-005 | 보안 | Spring Security로 모든 경로를 로그인 없이 허용하고, 폼 로그인·HTTP Basic·로그아웃 기능을 꺼야 한다. | 로그인 페이지로 리다이렉트되지 않음 | PRD 9절 | Test |
| REQ-NF-006 | 보안 | 모든 POST 요청에 CSRF 토큰 검증을 적용해야 한다. | 토큰 없는 POST는 403 | PRD 7절 | Test |
| REQ-NF-007 | 보안 | 작성 비밀번호는 `PasswordEncoder`(BCrypt, 강도 `app.security.bcrypt-strength`, 기본 10)로 해시해 저장하고, 평문을 저장·로그·화면에 남기지 않아야 한다. | DB에 평문 없음 | PRD 7절 | Test |
| REQ-NF-008 | 보안 | 사용자 입력은 항상 HTML 이스케이프해서 출력해야 한다 (`th:utext` 사용 금지). | `<script>` 입력이 글자로 표시됨 | FR-7 | Test |
| REQ-NF-009 | 보안 | Spring Boot가 기본 사용자와 생성된 비밀번호를 만들거나 로그에 출력하지 않아야 한다. | 시작 로그에 "generated security password" 없음 | - | Inspection |

- 요청 횟수 제한과 비밀번호 실패 잠금은 두지 않는다 (PRD 결정).
- 보안 헤더는 Spring Security 기본값을 쓴다.

### 6.3 설정 외부화

| ID | 분류 | 요구사항 | 측정 기준 | 출처 | 검증 방법 |
|---|---|---|---|---|---|
| REQ-NF-010 | 설정 | 환경마다 다른 값(DB 접속, 포트, 프로필, 풀 크기, 로그 레벨)은 환경 변수로만 주입하고, 비밀 값은 저장소에 커밋하지 않아야 한다. | 저장소에 비밀 값 없음 | 사용자 요청 | Inspection |
| REQ-NF-011 | 설정 | 업무 설정값은 `app.*` 설정 프로퍼티로 정의하고, 타입이 있는 설정 클래스(`@ConfigurationProperties`)로 한곳에서 읽어야 한다. | 코드에 해당 값 리터럴 없음 | 사용자 요청 | Inspection |
| REQ-NF-012 | 설정 | 화면 문구와 오류 메시지는 `messages.properties`에 두고 템플릿·코드에 직접 쓰지 않아야 한다. | 템플릿에 한글 리터럴 없음 | 사용자 요청 | Inspection |
| REQ-NF-013 | 설정 | 입력 길이 제한은 DB 컬럼 길이와 맞물리므로 설정으로 빼지 않고, 폼 클래스의 검증 애너테이션에 5절과 같은 값으로 한 번만 정의한다. | 5절과 값 일치 | - | Inspection |
| REQ-NF-014 | 설정 | `prod`에서 필수 환경 변수(`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)가 없으면 기본값으로 대체하지 않고 시작에 실패해야 한다. | 누락 시 시작 실패 | - | Test (수동) |

**환경 변수 목록**

| 변수 | 필수 | 기본값 | 용도 |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod`에서 예 | `local` | 실행 프로필 |
| `PORT` | 아니오 | `8080` | HTTP 포트 (Render가 주입) |
| `DB_URL` | `prod`에서 예 | 없음 | JDBC URL |
| `DB_USERNAME` | `prod`에서 예 | 없음 | DB 사용자 |
| `DB_PASSWORD` | `prod`에서 예 | 없음 | DB 비밀번호 |
| `DB_POOL_MAX_SIZE` | 아니오 | `5` | 커넥션 풀 최대 크기 |
| `JAVA_TOOL_OPTIONS` | 아니오 | `-XX:MaxRAMPercentage=75` (이미지 기본값) | JVM 메모리 옵션 |
| `LOGGING_LEVEL_ROOT` | 아니오 | `INFO` | 로그 레벨 (Spring 기본 바인딩) |

**`app.*` 설정 목록** (기본값은 `application.yaml`에 둔다)

| 키 | 기본값 | 용도 |
|---|---|---|
| `app.site.name` | `맛집 리뷰` | 머리글과 `<title>` |
| `app.site.description` | `솔직한 맛집 후기를 나누는 곳` | 기본 `<meta name="description">` |
| `app.pagination.page-size` | `20` | 목록 페이지 크기 |
| `app.store.search-limit` | `10` | 가게 찾기 최대 결과 수 |
| `app.store.categories` | `한식, 중식, 일식, 양식, 아시안, 분식, 카페·디저트, 주점, 기타` | 카테고리 목록과 순서 |
| `app.display.time-zone` | `Asia/Seoul` | 화면 표시 시간대 |
| `app.security.bcrypt-strength` | `10` | BCrypt 강도 |

- 각 `app.*` 값은 Spring의 기본 바인딩 규칙에 따라 환경 변수(예: `APP_PAGINATION_PAGE_SIZE`)로 덮어쓸 수 있어야 한다.

### 6.4 배포

| ID | 분류 | 요구사항 | 측정 기준 | 출처 | 검증 방법 |
|---|---|---|---|---|---|
| REQ-NF-020 | 배포 | 저장소 루트의 `Dockerfile`은 멀티 스테이지 빌드로, 빌드 단계에서 Gradle wrapper로 실행 가능한 jar를 만들고 실행 단계에는 JRE 17과 jar만 담아야 한다. | `docker build` 성공, 실행 이미지에 JDK·소스 없음 | 사용자 요청 | Test |
| REQ-NF-021 | 배포 | 실행 이미지는 root가 아닌 사용자로 앱을 실행하고, 기본 `SPRING_PROFILES_ACTIVE=prod`와 `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`를 가져야 하며, 두 값 모두 환경 변수로 덮어쓸 수 있어야 한다. | `docker inspect`로 확인 | 사용자 요청 | Inspection |
| REQ-NF-022 | 배포 | 이미지 빌드 시 테스트는 건너뛰고(테스트는 로컬·CI에서 수행), 의존성 다운로드 단계를 소스 복사보다 먼저 두어 레이어 캐시를 활용해야 한다. | 소스만 바꾼 재빌드에서 의존성 레이어 재사용 | 1시간 제약 | Inspection |
| REQ-NF-023 | 배포 | `.dockerignore`로 `build/`, `.gradle/`, `.idea/`, `.git/`, `docs/`, `.env*`를 빌드 컨텍스트에서 제외해야 한다. `.env*`는 `.gitignore`에도 추가한다. | 파일 존재 | REQ-NF-010 | Inspection |
| REQ-NF-024 | 배포 | README 또는 `docs/deploy.md`에 Neon DB 생성, JDBC URL 변환, Render 서비스 생성(Docker, 헬스 체크 경로, 환경 변수) 절차를 적어야 한다. | 문서 존재 | 사용자 요청 | Inspection |
| REQ-NF-025 | 배포 | 컨테이너는 512MB 메모리 한도에서 시작하고 요청을 처리해야 한다. | 메모리 초과로 재시작되지 않음 | 2.5절 | Demonstration |
| REQ-NF-026 | 배포 | Render Blueprint(`render.yaml`)로 서비스 설정을 코드화할 수 있다. 비밀 값은 `sync: false`로 두어 파일에 적지 않는다. | 파일 존재 | 사용자 요청 | Inspection (Could) |

### 6.5 테스트

| ID | 분류 | 요구사항 | 측정 기준 | 출처 | 검증 방법 |
|---|---|---|---|---|---|
| REQ-NF-030 | 테스트 | `./gradlew test`는 외부 DB나 네트워크 없이 H2만으로 통과해야 한다. | 오프라인에서 통과 | PRD 9절 | Test |
| REQ-NF-031 | 테스트 | Must 기능 요구사항마다 정상 흐름 1개 이상, 주요 예외 흐름(검증 실패, 비밀번호 불일치, 404) 1개 이상의 MockMvc 테스트가 있어야 한다. | 추적 매트릭스 기준 누락 없음 | - | Inspection |
| REQ-NF-032 | 테스트 | 배포 전에 Neon 개발 브랜치에 `prod` 프로필로 연결해 마이그레이션과 검색이 동작하는지 수동 확인한다. | 확인 기록 | PRD 10절 | Demonstration |

## 7. 추적 매트릭스

| PRD 요구사항 | SRS 요구사항 |
|---|---|
| FR-1 리뷰 작성 | REQ-FUNC-001, 004, REQ-IF-012, 013, REQ-DATA-002, REQ-NF-007 |
| FR-2 가게 정보 | REQ-FUNC-001~004, REQ-DATA-001 |
| FR-3 리뷰 조회 | REQ-FUNC-005, 006, REQ-IF-010, 011, 014, REQ-NF-003 |
| FR-4 리뷰 수정·삭제 | REQ-FUNC-007, 008, REQ-IF-015~017, REQ-DATA-003 |
| FR-5 가게 상세 | REQ-FUNC-009, REQ-IF-020 |
| FR-6 검색 | REQ-FUNC-010, REQ-IF-011, REQ-NF-002 |
| FR-7 댓글 | REQ-FUNC-011, 012, REQ-IF-018, 019, REQ-DATA-003, REQ-NF-008 |
| PRD 7절 성능 | REQ-NF-001~004 |
| PRD 7절 보안 | REQ-FUNC-013, REQ-NF-005~009 |
| PRD 7절 SEO·접근성·호환성 | REQ-IF-001 |
| PRD 9절 DB 환경·Neon | REQ-IF-030, REQ-DATA-004, REQ-NF-030, 032 |
| PRD 9절 Render·Docker 배포 | REQ-IF-031, REQ-NF-020~026 |
| 설정 외부화 (사용자 요청) | REQ-NF-010~014 |

## 8. 미결 사항

- [x] 기본 사이트 이름(`app.site.name`)과 설명 문구 → 6.3절 기본값 사용 (설정으로 언제든 변경 가능)

### 임의로 정한 값

PRD에 없어서 이 문서에서 기본값으로 정한 항목이다. 바꾸려면 알려 주면 된다.

- 가게 이름 50자, 주소 200자, 가게 찾기 결과 최대 10개
- 리뷰 수정 범위는 제목·본문·별점 (가게·닉네임은 변경 불가)
- 기존 가게와 이름·주소가 같으면 새로 입력한 지역·카테고리는 무시
- 검증 실패 시 HTTP 200으로 폼 재표시, 삭제 비밀번호 불일치 시 상세로 리다이렉트 후 1회 메시지
- 검색어 초과 길이는 오류 대신 잘라서 사용, 잘못된 필터 값은 오류 대신 무시
- 평균 별점 HALF_UP 반올림, 화면 시각 `Asia/Seoul` `yyyy-MM-dd HH:mm`
- 헬스 체크용 Spring Boot Actuator 도입 (`health`만 노출)

## 부록 A. 추가 의존성

`build.gradle` 변경은 사용자가 직접 한다. Spring Boot 4는 기능별 스타터로 나뉘어 있으므로 다음이 필요하다.

| 용도 | 의존성 |
|---|---|
| JPA | `spring-boot-starter-data-jpa` |
| 입력 검증 | `spring-boot-starter-validation` |
| 보안 | `spring-boot-starter-security`, `thymeleaf-extras-springsecurity6` (필요 시), 테스트용 `spring-boot-starter-security-test` |
| 마이그레이션 | `spring-boot-starter-flyway`, `flyway-database-postgresql` |
| 헬스 체크 | `spring-boot-starter-actuator` |
| DB 드라이버 | `runtimeOnly` `com.h2database:h2`, `org.postgresql:postgresql` |

- 정확한 아티팩트 이름은 Spring Boot 4.1.1 문서에서 확인한다. H2는 `local`·`test`에서만 쓰지만, 같은 jar로 모든 프로필을 실행하므로 `runtimeOnly`로 둔다.

## 부록 B. 1시간 구현 순서

AI 에이전트에게 단계별로 맡기고, 단계마다 `./gradlew test`로 확인한다.

| 순서 | 시간 | 작업 | 관련 요구사항 |
|---|---|---|---|
| 1 | 10분 | 의존성 추가, `application.yaml`과 프로필별 설정, `app.*` 설정 클래스, 메시지 파일, Security 설정, 마이그레이션 | 부록 A, REQ-NF-004~014, REQ-DATA-001~004 |
| 2 | 15분 | 리뷰 작성·목록·상세, 가게 찾기, 공통 레이아웃과 오류 페이지 | REQ-FUNC-001~006, 013, REQ-IF-001 |
| 3 | 10분 | 리뷰 수정·삭제, 댓글 작성·삭제 | REQ-FUNC-007, 008, 011, 012 |
| 4 | 10분 | 검색, 가게 상세 | REQ-FUNC-009, 010 |
| 5 | 10분 | Dockerfile, `.dockerignore`, 배포 문서, 로컬에서 `docker build`·`docker run` 확인 | REQ-NF-020~025 |
| 6 | 5분 | Render 배포, 헬스 체크와 주요 흐름 확인 | REQ-IF-030, 031, REQ-NF-032 |

- 시간이 모자라면 Should·Could 요구사항(REQ-FUNC-009, REQ-NF-002, REQ-NF-026)을 먼저 뺀다.

## 변경 이력

| 날짜 | 변경 내용 |
|---|---|
| 2026-09-17 | 최초 작성 (PRD-001 기준, Render·Docker 배포와 설정 외부화 요구 반영) |
| 2026-09-17 | 검토 완료, 임의로 정한 값과 기본 사이트 이름 확정, 상태를 Approved로 변경 |
