# DESIGN.md

taste-review 화면의 디자인 규칙이다. 모든 Thymeleaf 템플릿과 CSS는 이 문서를 따른다.
이 문서는 `/design` 스킬로 관리하며, 규칙을 바꾸면 맨 아래 `변경 이력`에 남긴다.

## 1. 원칙

1. **모바일 우선**: 너비 360px 화면을 기준으로 먼저 설계하고, 넓은 화면은 미디어 쿼리로 확장한다.
2. **따뜻한 색감**: 크림색 바탕, 짙은 갈색 글자, 번트 오렌지 강조색으로 음식과 식당의 온기를 전한다.
3. **Pretendard**: 모든 글자는 Pretendard를 쓴다.
4. **8px 그리드**: 모든 패딩, 마진, 간격(gap)과 요소 크기는 8px의 배수다.
5. **토큰만 사용**: 색, 간격, 글자 크기는 2~5절의 CSS 변수로만 쓰고, 템플릿이나 CSS에 값을 직접 쓰지 않는다.
6. **단순함**: JavaScript 없이 동작하는 HTML과 CSS 하나(`static/css/app.css`)로 만든다 ([ADR-001](docs/adr/ADR-001-use-ssr-with-thymeleaf.md)).

## 2. 색상

모든 조합은 WCAG 2.1 AA 기준을 확인했다. 글자는 4.5:1 이상, 입력란 테두리와 포커스 표시는 3:1 이상이다.

| 토큰 | 값 | 용도 | 대비 (크림 바탕 / 흰 바탕) |
|---|---|---|---|
| `--color-bg` | `#FFF8F1` | 페이지 바탕 (크림) | - |
| `--color-surface` | `#FFFFFF` | 카드, 입력란 바탕 | - |
| `--color-surface-muted` | `#FBEEE2` | 머리글, 댓글 영역, 선택된 가게 | - |
| `--color-border` | `#EBD9C8` | 카드 테두리, 구분선 (장식용) | - |
| `--color-border-strong` | `#A8836A` | 입력란 테두리 | 3.26 / 3.43 |
| `--color-text` | `#3B2A20` | 본문, 제목 | 12.97 / 13.65 |
| `--color-text-muted` | `#7A5F4E` | 닉네임, 날짜, 보조 설명 | 5.58 / 5.88 |
| `--color-primary` | `#C2410C` | 주요 버튼, 링크 | 4.92 / 5.18 |
| `--color-primary-hover` | `#9A3412` | 버튼·링크 hover, active | 6.94 / 7.31 |
| `--color-on-primary` | `#FFFFFF` | 주요 버튼 글자 | 버튼 위 5.18 |
| `--color-star` | `#B45309` | 별점 별 | 4.77 / 5.02 |
| `--color-focus` | `#EA580C` | 키보드 포커스 테두리 | 3.38 / 3.56 |
| `--color-danger` | `#B42318` | 오류 메시지, 삭제 버튼 글자 | 6.25 / 6.57 |
| `--color-danger-bg` | `#FDECEA` | 오류 안내 상자 바탕 | 글자 5.75 |
| `--color-success` | `#35702F` | 완료 메시지 | 5.68 / 5.98 |
| `--color-success-bg` | `#EAF4E8` | 완료 안내 상자 바탕 | 글자 5.30 |

- 색만으로 의미를 전하지 않는다. 오류는 색과 함께 문구를, 별점은 별과 함께 숫자(예: `★ 4.5`)를 보여 준다.
- 삭제처럼 되돌릴 수 없는 동작의 버튼은 `--color-danger` 글자의 테두리 버튼으로 만든다. 채운 빨간 버튼은 쓰지 않는다.
- 다크 모드는 MVP에서 지원하지 않는다.

## 3. 타이포그래피

### 3.1 폰트

- 메인 폰트: **Pretendard** (Variable, 동적 서브셋).
- 불러오기: 공통 레이아웃 `<head>`에서 jsDelivr CDN의 CSS를 연결한다.
  `https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css`
  - 버전은 URL에 고정한다. 올릴 때는 이 문서와 변경 이력을 함께 고친다.
- 폰트 스택:
  `"Pretendard Variable", Pretendard, -apple-system, BlinkMacSystemFont, system-ui, "Apple SD Gothic Neo", "Noto Sans KR", "Malgun Gothic", sans-serif`
- 폰트 로딩 중에도 글자가 보이도록 CDN CSS의 `font-display: swap`을 그대로 쓴다.

### 3.2 크기와 굵기

| 토큰 | 크기 / 줄 높이 | 굵기 | 용도 |
|---|---|---|---|
| `--text-xs` | 12px / 16px | 400 | 배지, 보조 표기 (최소 크기) |
| `--text-sm` | 14px / 24px | 400 | 닉네임, 날짜, 도움말, 오류 메시지 |
| `--text-md` | 16px / 24px | 400 | 본문, 입력란, 버튼 (기본) |
| `--text-lg` | 20px / 32px | 600 | 카드 제목, 섹션 제목 |
| `--text-xl` | 24px / 32px | 700 | 페이지 제목 (모바일) |
| `--text-2xl` | 32px / 40px | 700 | 페이지 제목 (960px 이상) |

- 줄 높이는 8px 배수로 맞춰 세로 리듬을 지킨다.
- 입력란(`input`, `textarea`, `select`)의 글자는 16px 미만으로 줄이지 않는다. iOS Safari가 16px 미만 입력란에 포커스하면 화면을 확대한다.
- 리뷰 본문은 `white-space: pre-line`으로 줄바꿈을 보여 주고, 긴 단어는 `overflow-wrap: anywhere`로 줄을 넘긴다.
- 한국어 본문은 `word-break: keep-all`로 단어 중간에서 줄이 바뀌지 않게 한다.

## 4. 간격 (8px 그리드)

| 토큰 | 값 | 주 용도 |
|---|---|---|
| `--space-1` | 8px | 아이콘과 글자 사이, 라벨과 입력란 사이 |
| `--space-2` | 16px | 카드 안쪽 패딩(모바일), 모바일 좌우 여백, 폼 필드 사이 |
| `--space-3` | 24px | 카드 안쪽 패딩(600px 이상), 카드 사이 |
| `--space-4` | 32px | 섹션 사이 |
| `--space-5` | 40px | 페이지 제목과 본문 사이(960px 이상) |
| `--space-6` | 48px | 페이지 위아래 여백, 머리글·바닥글과 본문 사이 |

- 패딩, 마진, `gap`, `width`, `height`, `min-height`는 위 토큰이나 8px 배수 값만 쓴다.
- 예외는 간격이 아닌 값뿐이다: 테두리 두께(1px, 포커스 2px), 모서리 반경(5절), 글자 크기(3절).
- 버튼·입력란 안의 글자를 가운데 맞추기 위해 4px 같은 값을 쓰지 않는다. 높이(`min-height`)와 줄 높이로 맞춘다.

## 5. 모서리, 그림자, 테두리

| 토큰 | 값 | 용도 |
|---|---|---|
| `--radius-sm` | 8px | 버튼, 입력란, 배지 |
| `--radius-md` | 16px | 카드, 안내 상자 |
| `--radius-full` | 9999px | 카테고리 배지 |
| `--shadow-card` | `0 1px 2px rgba(59, 42, 32, 0.08)` | 카드 (옅게만) |
| `--border-width` | 1px | 기본 테두리 |
| `--focus-ring` | `2px solid var(--color-focus)`, `outline-offset: 2px` | 모든 포커스 가능 요소 |

- `outline: none`으로 포커스 표시를 없애지 않는다. `:focus-visible`에 `--focus-ring`을 쓴다.

## 6. 레이아웃과 반응형

### 6.1 브레이크포인트 (모바일 우선, `min-width`)

| 이름 | 조건 | 변화 |
|---|---|---|
| 기본 | 360px~ | 한 열, 좌우 여백 `--space-2` |
| `sm` | `min-width: 600px` | 좌우 여백 `--space-3`, 카드 패딩 `--space-3`, 검색 필터 두 열 |
| `md` | `min-width: 960px` | 본문 최대 너비 고정, 페이지 제목 `--text-2xl`, 검색 필터 한 줄 |

- 본문 컨테이너: `max-width: 960px`, 가운데 정렬. 리뷰 본문과 폼은 읽기 편하도록 `max-width: 720px`.
- 너비 360px에서 가로 스크롤이 생기지 않아야 한다 ([SRS-001](docs/srs/SRS-001-restaurant-review-site.md) REQ-IF-001).
- 공통 레이아웃에 `<meta name="viewport" content="width=device-width, initial-scale=1">`을 넣는다. 확대(`user-scalable`)를 막지 않는다.
- 레이아웃은 Flexbox와 Grid로 만들고, 고정 너비(`width: 400px` 등) 대신 `max-width`와 `%`를 쓴다.

### 6.2 터치

- 버튼, 링크, 체크 가능한 요소의 터치 영역은 최소 48×48px이다.
- 터치 요소 사이는 최소 `--space-1`(8px) 띄운다.
- 모바일에서 주요 버튼(등록, 검색)은 폼 너비를 꽉 채운다(`width: 100%`). 600px 이상에서는 내용 너비로 줄인다.

## 7. 컴포넌트

### 7.1 머리글

- 바탕 `--color-surface-muted`, 아래 테두리 `--color-border`, `min-height: 64px`, 좌우 패딩은 컨테이너 여백과 같다.
- 왼쪽: 사이트 이름(`app.site.name`, `--text-lg`, 홈 링크). 오른쪽: "리뷰 쓰기" 주요 버튼.
- 모바일에서 검색 폼은 머리글 아래 한 줄로 내려 전체 너비를 쓴다.

### 7.2 버튼

| 종류 | 모양 | 용도 |
|---|---|---|
| 주요 | 바탕 `--color-primary`, 글자 `--color-on-primary`, hover 시 바탕 `--color-primary-hover` | 등록, 검색, 수정 저장 |
| 보조 | 바탕 `--color-surface`, 테두리 `--color-border-strong`, 글자 `--color-text` | 취소, 가게 찾기, 페이지 이동 |
| 위험 | 바탕 `--color-surface`, 테두리·글자 `--color-danger` | 리뷰·댓글 삭제 |

- 공통: `min-height: 48px`, 좌우 패딩 `--space-2`, `--radius-sm`, `--text-md`, 굵기 600.
- 비활성 상태는 만들지 않는다 (JavaScript가 없으므로 서버 검증으로 처리).

### 7.3 폼

- 필드 구조: `label` → 입력란 → 도움말 또는 오류 메시지. 라벨과 입력란 사이 `--space-1`, 필드 사이 `--space-2`.
- 입력란: 바탕 `--color-surface`, 테두리 `--border-width` `--color-border-strong`, `--radius-sm`, `min-height: 48px`, 좌우 패딩 `--space-2`, 글자 `--text-md`.
- `textarea`: `min-height: 160px`(리뷰 본문), `80px`(댓글), 세로 크기만 조절 가능.
- 필수 항목은 라벨 뒤에 "(필수)" 문구를 붙인다. 별표(*)만 쓰지 않는다.
- 오류: 입력란 테두리 `--color-danger`, 아래에 `--text-sm` `--color-danger` 오류 문구. `aria-invalid="true"`와 `aria-describedby`로 오류 문구를 연결한다.
- 폼 상단에 오류가 있으면 `--color-danger-bg` 안내 상자로 "입력 내용을 확인해 주세요"를 보여 준다.
- 비밀번호 입력란 아래에는 "잊으면 수정·삭제할 수 없어요" 도움말을 `--text-sm` `--color-text-muted`로 둔다.
- 별점 입력: 1~5 라디오 버튼을 "★ 1" ~ "★ 5" 라벨과 함께 가로로 배치하고, 각 선택지는 48×48px 이상이다.

### 7.4 리뷰 카드 (목록)

- 바탕 `--color-surface`, 테두리 `--color-border`, `--radius-md`, `--shadow-card`, 패딩 `--space-2`(600px 이상 `--space-3`).
- 카드 사이 간격 `--space-2`(600px 이상 `--space-3`). 목록은 한 열이며, 960px 이상에서도 한 열을 유지한다.
- 내용 순서: 제목(`--text-lg`, 상세 링크) → 가게 이름과 카테고리 배지 → 별점 → 닉네임·작성일·댓글 수(`--text-sm`, `--color-text-muted`).
- 카드 전체가 아니라 제목만 링크로 만든다.

### 7.5 별점 표시

- 형식: `★` 문자(색 `--color-star`) + 숫자(`--color-text`). 예: `★ 4.7`.
- 별은 장식이므로 `aria-hidden="true"`를 붙이고, 숫자에 "별점 4.7점" 같은 접근 가능한 이름을 준다.
- 이미지나 아이콘 폰트를 쓰지 않는다.

### 7.6 배지

- 카테고리와 지역에 쓴다. 바탕 `--color-surface-muted`, 글자 `--color-text-muted` `--text-xs`, `--radius-full`, 좌우 패딩 `--space-1`, `min-height: 24px`.

### 7.7 리뷰 상세와 댓글

- 리뷰 상세: 제목(`--text-xl` / 960px 이상 `--text-2xl`) → 메타 정보 → 가게 정보 상자(`--color-surface-muted`, `--radius-md`) → 본문 → 수정 링크·삭제 폼.
- 삭제 폼은 비밀번호 입력란과 위험 버튼을 한 줄에 두고, 360px에서는 두 줄로 내려간다.
- 댓글 영역(`id="comments"`): 바탕 `--color-surface-muted`, 패딩 `--space-2`, 댓글 사이 구분선 `--color-border`, 댓글 사이 간격 `--space-2`.
- 댓글 삭제 폼은 각 댓글 아래에 작게(보조 크기 입력란 + 위험 버튼) 둔다. 입력란과 버튼 높이는 그대로 48px이다.

### 7.8 안내 상자 (flash 메시지)

- 성공: 바탕 `--color-success-bg`, 글자 `--color-success`. 오류: 바탕 `--color-danger-bg`, 글자 `--color-danger`.
- 패딩 `--space-2`, `--radius-md`, 본문 위 `--space-2` 간격. `role="status"`(성공) 또는 `role="alert"`(오류)를 붙인다.

### 7.9 페이지 이동

- "이전", 페이지 번호, "다음"을 보조 버튼 모양의 링크로 가운데 정렬한다. 각 링크 48×48px 이상, 사이 간격 `--space-1`.
- 현재 페이지는 주요 버튼 색으로 표시하고 `aria-current="page"`를 붙인다.
- 360px에서는 번호를 현재 페이지 앞뒤 2개까지만 보여 준다.

### 7.10 빈 상태와 오류 페이지

- 빈 상태(검색 결과 없음, 리뷰 없음): 가운데 정렬, 위아래 패딩 `--space-6`, 안내 문구(`--text-md`)와 보조 버튼 하나.
- 오류 페이지(404, 403, 500): 공통 레이아웃 안에 상태 코드(`--text-2xl`), 설명 한 줄, "홈으로" 주요 버튼.

## 8. 접근성 체크리스트

- [ ] 모든 입력란에 연결된 `label`이 있다.
- [ ] 모든 포커스 가능한 요소에 포커스 표시가 보인다.
- [ ] 글자 대비 4.5:1, 입력란 테두리 3:1 이상 (2절 토큰만 쓰면 만족).
- [ ] 색만으로 의미를 전하지 않는다.
- [ ] 페이지마다 `h1`이 하나다.
- [ ] `<html lang="ko">`가 있다.
- [ ] 브라우저 확대 200%에서도 내용이 잘리지 않는다.

## 9. 리뷰 체크리스트

템플릿이나 CSS를 리뷰할 때 확인한다.

- [ ] 색, 간격, 글자 크기를 토큰 대신 값으로 직접 쓰지 않았다 (`#`, `px` 검색).
- [ ] 패딩, 마진, `gap`, 요소 크기가 8px 배수다.
- [ ] 미디어 쿼리가 `min-width` 방식이고 브레이크포인트가 600px, 960px이다.
- [ ] 360px에서 가로 스크롤이 없고, 터치 영역이 48px 이상이다.
- [ ] 입력란 글자가 16px 이상이다.
- [ ] 폰트가 Pretendard 스택이다.
- [ ] 인라인 `style` 속성을 쓰지 않았다.
- [ ] 8절 접근성 체크리스트를 만족한다.

## 변경 이력

| 날짜 | 변경 내용 |
|---|---|
| 2026-09-17 | 최초 작성 (모바일 우선, 따뜻한 색감, Pretendard, 8px 그리드) |
