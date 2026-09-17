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
7. **한국형 앱의 절제된 완성도(토스·카카오·네이버 참고, v3)**: 선(테두리)으로 구획을 나누지 않고 여백과 그림자로 나눈다. 카드·버튼은 크게 둥글리고, 핵심 숫자·제목은 굵고 크게, 화면마다 버튼과 강조 요소는 하나로 좁힌다. 색은 그대로 따뜻한 팔레트를 쓰되(2절), 구성 방식만 절제된 국내 앱 스타일을 따른다.

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
- **(v3) `--color-border`는 최소화한다.** 카드·상자의 구획은 원칙적으로 `--color-border` 테두리 대신 `--shadow-card`(그림자)나 `--color-surface`/`--color-surface-muted` 배경 대비로 나눈다. `--color-border`는 입력란 바깥 테두리(→ 실제로는 `--color-border-strong` 사용), `dl`의 항목 구분선, 댓글 카드 내부처럼 배경 대비만으로는 구분이 안 되는 좁은 자리에만 남긴다.

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
| `--text-hero` | 40px / 48px | 800(`--font-extrabold`) | 가게 평균 별점 같은 "핵심 숫자" 하나 (v3) |

- 줄 높이는 8px 배수로 맞춰 세로 리듬을 지킨다.
- 입력란(`input`, `textarea`, `select`)의 글자는 16px 미만으로 줄이지 않는다. iOS Safari가 16px 미만 입력란에 포커스하면 화면을 확대한다.
- 리뷰 본문은 `white-space: pre-line`으로 줄바꿈을 보여 주고, 긴 단어는 `overflow-wrap: anywhere`로 줄을 넘긴다.
- 한국어 본문은 `word-break: keep-all`로 단어 중간에서 줄이 바뀌지 않게 한다.
- **(v3) 제목·핵심 숫자는 타이트하게.** `--text-xl`, `--text-2xl`, `--text-hero`와 `--font-extrabold`(800)를 쓰는 곳은 `letter-spacing: -0.02em`(`--tracking-tight` 토큰)을 함께 준다. 자간 값은 배수 단위가 없는 `em`이라 4절 8px 규칙 대상이 아니다. 본문(`--text-md`, `--text-sm`)에는 쓰지 않는다.

## 4. 간격 (8px 그리드)

| 토큰 | 값 | 주 용도 |
|---|---|---|
| `--space-1` | 8px | 아이콘과 글자 사이, 라벨과 입력란 사이 |
| `--space-2` | 16px | 모바일 좌우 여백, 폼 필드 사이, 배지·작은 상자 패딩 |
| `--space-3` | 24px | **카드 안쪽 패딩(모바일 포함 기본값, v3)**, 카드 사이 |
| `--space-4` | 32px | 섹션 사이, 600px 이상 카드 안쪽 패딩 |
| `--space-5` | 40px | 페이지 제목과 본문 사이(960px 이상) |
| `--space-6` | 48px | 페이지 위아래 여백, 머리글·바닥글과 본문 사이 |
| `--space-7` | 56px | 핵심 CTA 버튼 높이 (v3) |

- 패딩, 마진, `gap`, `width`, `height`, `min-height`는 위 토큰이나 8px 배수 값만 쓴다.
- 예외는 간격이 아닌 값뿐이다: 테두리 두께(1px, 포커스 2px), 모서리 반경(5절), 글자 크기(3절).
- 버튼·입력란 안의 글자를 가운데 맞추기 위해 4px 같은 값을 쓰지 않는다. 높이(`min-height`)와 줄 높이로 맞춘다.

## 5. 모서리, 그림자, 테두리

| 토큰 | 값 | 용도 |
|---|---|---|
| `--radius-sm` | 8px | 입력란, 보조·위험 버튼, 배지 |
| `--radius-md` | 16px | 안내 상자, 가게 정보 상자, 댓글 카드 |
| `--radius-lg` | 24px | 리뷰 카드, 평균 별점 요약 상자 (v3) |
| `--radius-full` | 9999px | 카테고리 배지, 기본(주요) 버튼 — 알약 모양 (v3) |
| `--shadow-card` | `0 2px 8px rgba(59, 42, 32, 0.06)` | 카드 (기본, v3에서 더 옅고 넓게 조정) |
| `--shadow-card-hover` | `0 8px 20px rgba(59, 42, 32, 0.12)` | 카드 hover·focus-within (떠 있는 느낌으로 클릭 가능함을 알림, v3에서 더 또렷하게) |
| `--border-width` | 1px | 입력란 테두리, 좁은 구분선(2절) |
| `--focus-ring` | `2px solid var(--color-focus)`, `outline-offset: 2px` | 모든 포커스 가능 요소 |

- `outline: none`으로 포커스 표시를 없애지 않는다. `:focus-visible`에 `--focus-ring`을 쓴다.
- 카드처럼 클릭 가능한 영역은 hover·포커스 시 `--shadow-card`에서 `--shadow-card-hover`로, `transform: translateY(-2px)`로 살짝 떠오르게 해 "눌러도 된다"는 신호를 준다. `prefers-reduced-motion: reduce`에서는 transform을 끈다.
- **(v3) 카드는 테두리 없이 그림자만으로 띄운다.** `--color-border`를 카드 바깥 테두리에 쓰지 않는다(2절).

## 6. 레이아웃과 반응형

### 6.1 브레이크포인트 (모바일 우선, `min-width`)

| 이름 | 조건 | 변화 |
|---|---|---|
| 기본 | 360px~ | 한 열, 좌우 여백 `--space-2` |
| `sm` | `min-width: 600px` | 좌우 여백 `--space-3`, 카드 패딩 `--space-3`, 검색 필터 두 열 |
| `md` | `min-width: 960px` | 본문 최대 너비 고정, 페이지 제목 `--text-2xl`, 검색 필터 한 줄 |

- 본문 컨테이너: `max-width: 960px`, 가운데 정렬. 리뷰 본문과 폼은 읽기 편하도록 `max-width: 720px`(`.read` 토큰).
  - **적용 범위(필수)**: 리뷰 작성 폼(`reviews/new`), 리뷰 수정 폼(`reviews/edit`), 리뷰 상세의 제목·메타·가게 정보·본문·삭제 폼(`reviews/detail`)에 `.read`를 실제로 씌운다. 지금은 정의만 있고 어디에도 적용되지 않아, 960px 이상 화면에서 리뷰 본문 한 줄이 지나치게 길어져 읽기 어렵다. 목록·검색·페이지 이동, 댓글 영역은 폭이 넓어도 되므로 `.read`를 적용하지 않는다.
- 너비 360px에서 가로 스크롤이 생기지 않아야 한다 ([SRS-001](docs/srs/SRS-001-restaurant-review-site.md) REQ-IF-001).
- 공통 레이아웃에 `<meta name="viewport" content="width=device-width, initial-scale=1">`을 넣는다. 확대(`user-scalable`)를 막지 않는다.
- 레이아웃은 Flexbox와 Grid로 만들고, 고정 너비(`width: 400px` 등) 대신 `max-width`와 `%`를 쓴다.

### 6.2 터치

- 버튼, 링크, 체크 가능한 요소의 터치 영역은 최소 48×48px이다.
- 터치 요소 사이는 최소 `--space-1`(8px) 띄운다.
- 모바일에서 주요 버튼(등록, 검색)은 폼 너비를 꽉 채운다(`width: 100%`). 600px 이상에서는 내용 너비로 줄인다.

## 7. 컴포넌트

### 7.1 머리글 (v3: 최소화)

- 바탕 `--color-bg`(카드·본문과 같은 배경, 테두리 없음). 아래 구획은 테두리 대신 본문과의 여백(`--space-3`)만으로 나눈다. `min-height: 64px`, 좌우 패딩은 컨테이너 여백과 같다.
- 왼쪽: 사이트 이름(`app.site.name`, `--text-lg`, `--font-bold`, 홈 링크). 오른쪽: "리뷰 쓰기" 하나만 두는 주요 버튼(알약 모양, 7.2절).
- **검색창은 머리글에 두지 않는다.** 목록 화면(`/reviews`)에 이미 키워드+필터 검색이 있고(7.4절), 머리글에 똑같은 검색 폼을 또 두면 같은 기능이 두 곳에 보여 헷갈린다. 머리글의 사이트 이름 링크가 "검색·목록으로 가기" 역할을 겸한다.
- 이 변경으로 `templates/layout.html`의 `header` 프래그먼트에서 `site-header-search` 폼을 제거해야 한다(10절).

### 7.2 버튼

| 종류 | 모양 | 용도 |
|---|---|---|
| 주요(알약, v3) | 바탕 `--color-primary`, 글자 `--color-on-primary`, `--radius-full`, hover 시 바탕 `--color-primary-hover`, `active` 시 `transform: scale(0.98)` | 리뷰 쓰기, 등록, 검색, 수정 저장 |
| 보조 | 바탕 `--color-surface-muted`, 테두리 없음, 글자 `--color-text`, `--radius-sm` | 취소, 가게 찾기, 페이지 이동 |
| 위험 | 바탕 `--color-surface`, 테두리·글자 `--color-danger`, `--radius-sm` | 리뷰·댓글 삭제 |

- 공통: 좌우 패딩 `--space-3`, `--text-md`, 굵기 600, `:active`에서 미세한 눌림(`scale(0.98)`, `prefers-reduced-motion: reduce`면 생략).
- 높이: 화면당 하나뿐인 핵심 CTA(리뷰 쓰기, 등록·저장, 검색 실행)는 `min-height: 56px`(`--space-7`)로 크게. 그 외(취소, 가게 찾기, 페이지 이동, 삭제)는 `min-height: 48px`(`--space-6`)로 작게 — 화면 안에서 "가장 중요한 동작"이 크기로도 드러나게 한다.
- **(v3) 보조 버튼은 테두리 대신 옅은 배경으로 구분한다.** 지금처럼 `--color-border-strong` 테두리를 쓰지 않고 `--color-surface-muted` 채움만 쓴다(2절 "테두리 최소화" 원칙).
- 비활성 상태는 만들지 않는다 (JavaScript가 없으므로 서버 검증으로 처리).

### 7.3 폼

- 필드 구조: `label` → 입력란 → 도움말 또는 오류 메시지. 라벨과 입력란 사이 `--space-1`, 필드 사이 `--space-2`.
- 입력란: 바탕 `--color-surface`, 테두리 `--border-width` `--color-border-strong`, `--radius-sm`, `min-height: 48px`, 좌우 패딩 `--space-2`, 글자 `--text-md`.
- `textarea`: `min-height: 160px`(리뷰 본문), `80px`(댓글), 세로 크기만 조절 가능.
- 필수 항목은 라벨 뒤에 "(필수)" 문구를 붙인다. 별표(*)만 쓰지 않는다.
- 오류: 입력란 테두리 `--color-danger`, 아래에 `--text-sm` `--color-danger` 오류 문구. `aria-invalid="true"`와 `aria-describedby`로 오류 문구를 연결한다.
- 폼 상단에 오류가 있으면 `--color-danger-bg` 안내 상자로 "입력 내용을 확인해 주세요"를 보여 준다.
- 비밀번호 입력란 아래에는 "잊으면 수정·삭제할 수 없어요" 도움말을 `--text-sm` `--color-text-muted`로 둔다.

**별점 입력 (JavaScript 없는 CSS 전용 위젯)**

라디오 5개를 숫자 라벨과 나란히 늘어놓는 지금 방식은 무엇을 고르는지 한눈에 들어오지 않는다. 별 모양이 실제로 채워지는 CSS 전용 위젯으로 바꾼다.

- 마크업 순서는 5→1(라디오+라벨을 별점 높은 값부터 반복), 컨테이너에 `flex-direction: row-reverse`를 줘서 화면에는 1→5로 보이게 한다.
- 라디오는 `.visually-hidden`으로 숨기고, `label`을 `min-width`/`min-height` `--space-6`(48px) 정사각형 터치 영역의 별 글자(`★`, `--text-xl`)로 보여 준다. 기본 색은 `--color-border-strong`.
- `input:checked ~ label`과 `label:hover ~ label`(자기 자신 포함)에 `--color-star`를 줘서, 고른 값과 그 이전 별까지 채워지게 한다. `:focus-visible`인 라디오의 짝 라벨에는 `--focus-ring`을 준다.
- 라벨 안에 "1점" 같은 글자를 `.visually-hidden`으로 넣어 스크린 리더와 라디오 자체의 접근 가능한 이름을 유지한다. 값은 `aria-hidden` 없이 라디오 자체 이름으로 전달되므로 7.6절 규칙(장식용 별은 `aria-hidden`)과는 별개다.
- fieldset 레이아웃은 `flex-wrap` 대신 `inline-flex`로 한 줄에 고정해, 좁은 화면에서도 별 5개가 줄바꿈 없이 보이게 한다.

### 7.4 검색 폼 (목록 화면)

- 키워드 입력란은 항상 보이고, 지역·카테고리·최소 별점·정렬 필드는 `<details>`/`<summary>`(JavaScript 불필요)로 묶어 "상세 필터" 같은 이름으로 접어 둔다. 검색 조건이 하나라도 걸려 있으면(`search.filtered`) 서버 렌더링 시 `open` 속성을 붙여 펼친 채로 보여 준다.
- 이렇게 하면 모바일 첫 화면에서 검색창 하나만 보여 목록에 더 빨리 도달하고, 필터를 쓰던 사람은 결과 페이지에서도 계속 펼쳐진 상태를 본다.
- `summary`도 48px 이상 터치 영역과 `:focus-visible` 표시를 갖는다(기본 브라우저 스타일에 `--focus-ring`을 얹는다).

### 7.5 리뷰 카드 (목록)

- 바탕 `--color-surface`, **테두리 없음**, `--radius-lg`(v3), `--shadow-card`, 패딩 `--space-3`(모바일 포함 기본, v3), 600px 이상에서도 `--space-3`(더 키우지 않음 — 카드 자체가 이미 넉넉함).
- 카드 사이 간격 `--space-3`(v3, 모바일 포함). 목록은 한 열이며, 960px 이상에서도 한 열을 유지한다.
- 내용 순서: 제목(`--text-lg`, `--font-bold`, 상세 링크) → 가게 이름과 카테고리 배지 → 별점 → 닉네임·작성일·댓글 수(`--text-sm`, `--color-text-muted`).
- **카드 전체를 탭 가능하게 만든다.** `article`에 `position: relative`를 주고, 제목 링크에 `::after { content: ""; position: absolute; inset: 0; }`를 붙이는 "stretched link" 방식을 쓴다. 카드 안에 다른 링크가 없으므로 겹칠 위험이 없다. 모바일에서 제목 글자만 누르지 않아도 되므로 탭 실패가 줄어든다.
- hover·focus-within 시 `--shadow-card-hover`(5절)로 카드가 눌러도 되는 요소임을 보여 준다.
- 메타 정보(닉네임·작성일·댓글 수)는 값 사이에 가운뎃점(`·`)을 넣어 구분한다. 지금처럼 값만 나열하면 한 덩어리로 읽힌다.

### 7.6 별점 표시

- 형식: 채운 별과 빈 별 다섯 개를 나열하고(`★★★☆☆`처럼), 뒤에 숫자를 덧붙인다. 예: `★★★★☆ 4.0`, 소수 평균은 `★★★★☆ 4.7`(4개 채움, 반올림 없이 정수부만 채움). 채운 별은 `--color-star`, 빈 별은 `--color-border-strong`.
- 별 다섯 개는 장식이므로 감싸는 요소에 `aria-hidden="true"`를 붙이고, 그 옆의 숫자 텍스트에 "별점 4.7점" 같은 접근 가능한 이름을 준다. 지금처럼 별 하나 + 숫자만 쓰는 방식보다 한눈에 비교하기 쉽다.
- 이미지나 아이콘 폰트를 쓰지 않는다. `★`/`☆` 유니코드 글자만 쓴다.

### 7.7 배지

- 카테고리와 지역에 쓴다. 바탕 `--color-surface-muted`, 글자 `--color-text-muted` `--text-xs`, `--radius-full`, 좌우 패딩 `--space-1`, `min-height: 24px`.
- 카테고리 배지만 `--color-primary`를 옅게 쓴 강조 버전(바탕 `--color-danger-bg`가 아닌 `--color-surface-muted`에 글자 `--color-primary-hover`)으로 구분해, 지역 배지와 시각적으로 구별한다.

### 7.8 리뷰 상세와 댓글

- 리뷰 상세: 제목(`--text-xl` / 960px 이상 `--text-2xl`) → 메타 정보 → 가게 정보 상자(`--color-surface-muted`, `--radius-md`) → 본문 → 수정 링크·삭제 폼. `.read`(720px, 6.1절)로 감싼다.
- **가게 정보 상자**는 문단 나열 대신 정의 목록(`dl`)으로 "가게, 카테고리, 주소, 지역"을 라벨-값 구조로 보여 준다. 라벨은 `--text-sm` `--color-text-muted`, 값은 `--text-md` `--color-text`. 가게 이름은 `--text-lg`로 한 단계 키워 가게 상세 링크와 함께 둔다.
- 삭제 폼은 비밀번호 입력란과 위험 버튼을 한 줄에 두고, 360px에서는 두 줄로 내려간다.
- 댓글 영역(`id="comments"`): 바탕 `--color-surface-muted`, 패딩 `--space-2`.
- **댓글 하나하나를 카드로 만든다.** 지금처럼 위쪽 테두리 선으로만 구분하면 댓글이 몇 개인지, 어디서 끝나는지 훑어보기 어렵다. 각 댓글을 바탕 `--color-surface`, `--radius-sm`, 패딩 `--space-2`인 작은 카드로 만들고 카드 사이 간격 `--space-1`을 준다.
- 댓글 삭제 폼은 각 댓글 카드 안 아래쪽에 작게(보조 크기 입력란 + 위험 버튼) 둔다. 입력란과 버튼 높이는 그대로 48px이다.

### 7.9 가게 상세 요약

- **가게 정보 블록**: 리뷰 상세의 가게 정보 상자(7.8절)와 같은 구조를 가게 상세 페이지 자신에도 그대로 쓴다. 이름·카테고리·주소·지역을 `dl`(`dt`/`dd`)로 감싸고, `--color-surface-muted` 배경의 상자(`.store-box`와 같은 규칙)에 넣는다. 맨 텍스트를 그대로 나열하지 않는다 — 지금 이 부분이 스타일 없이 나열돼 있어 바로 아래의 평균 별점 상자와 대비되어 화면이 미완성으로 보인다.
- 그 아래에 "평균 별점 요약 상자"를 둔다: 바탕 `--color-surface-muted`, `--radius-lg`(v3), 패딩 `--space-4`.
- **요약 상자 내부는 반드시 레이아웃을 지정한다.** `display: flex`, `flex-wrap: wrap`, `align-items: baseline`, `gap: var(--space-2)`로 평균 숫자와 별 다섯 개를 한 줄에, 리뷰 수(`store-rating-count`)는 `flex-basis: 100%`로 다음 줄에 둔다. 자식 요소들을 아무 간격 없이 인라인으로 붙여 두지 않는다 — 지금 이 누락 때문에 "4.0★★★★☆별점 4.0점리뷰 1개"가 간격 없이 한 줄에 붙어 나온다.
- 평균 숫자는 `--text-hero`(v3, `--font-extrabold` + `--tracking-tight`)로 가장 크게, 별 다섯 개(7.6절 형식)를 그 옆에 둔다. 리뷰 수는 "리뷰 12개"처럼 `--text-sm` `--color-text-muted`로 다음 줄에 붙인다. 리뷰가 없으면 상자 대신 안내 문구만 보여 준다.
- 지금은 평균이 본문과 같은 크기 텍스트로 묻혀 있어, 가게를 고를 때 가장 중요한 정보가 눈에 띄지 않는다. 이 화면에서 시선이 가장 먼저 닿아야 할 숫자다.

### 7.10 안내 상자 (flash 메시지)

- 성공: 바탕 `--color-success-bg`, 글자 `--color-success`. 오류: 바탕 `--color-danger-bg`, 글자 `--color-danger`.
- 패딩 `--space-2`, `--radius-md`, 본문 위 `--space-2` 간격. `role="status"`(성공) 또는 `role="alert"`(오류)를 붙인다.

### 7.11 페이지 이동

- "이전", 페이지 번호, "다음"을 보조 버튼 모양의 링크로 가운데 정렬한다. 각 링크 48×48px 이상, 사이 간격 `--space-1`.
- 현재 페이지는 주요 버튼 색으로 표시하고 `aria-current="page"`를 붙인다.
- 360px에서는 번호를 현재 페이지 앞뒤 2개까지만 보여 준다.

### 7.12 빈 상태와 오류 페이지

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
- [ ] 리뷰 카드 전체가 탭 가능하고(stretched link), hover·focus 시 `--shadow-card-hover`가 보인다.
- [ ] 별점 입력이 CSS 전용 별 위젯이고, 별점 표시가 채운/빈 별 다섯 개 형식이다.
- [ ] 폼과 리뷰 본문이 `.read`(720px)로 감싸여 있다.
- [ ] (v3) 카드·머리글에 `--color-border` 테두리를 쓰지 않고 그림자·배경 대비로만 구분했다.
- [ ] (v3) 리뷰 카드가 `--radius-lg`, 패딩 `--space-3`(모바일 포함)이다.
- [ ] (v3) 주요 버튼이 알약 모양(`--radius-full`)이고, 화면당 핵심 CTA는 56px(`--space-7`), 나머지는 48px이다.
- [ ] (v3) 머리글에 검색 폼이 중복으로 있지 않다.

## 10. 구현에 반영 필요

v2 항목(카드 stretched link, CSS 전용 별점 위젯, 별 다섯 개 표시, `.read` 적용, 댓글 카드화, 가게 평균 별점 상자, 검색 폼 접기, 메타 구분자, 카테고리 배지 강조, 가게 정보 `dl`)은 이미 코드에 반영되어 있다.

아래는 이번 v3(한국형 앱 스타일) 개정으로 새로 손봐야 하는 항목이다.

| 규칙 | 무엇을 바꾸나 | 대상 파일 |
|---|---|---|
| 새 토큰 | `:root`에 `--radius-lg: 24px`, `--space-7: 56px`, `--text-hero`(40px/48px/800), `--font-extrabold: 800`, `--tracking-tight: -0.02em` 추가. `--shadow-card`/`--shadow-card-hover` 값을 5절 표대로 갱신 | `static/css/app.css` |
| 머리글 검색 제거 | `header` 프래그먼트에서 `site-header-search` 폼 삭제. 배경을 `--color-bg`로, 아래 테두리 삭제 | `templates/layout.html`, `static/css/app.css`(`.site-header`) |
| 카드 테두리 제거 + 확대 | `.review-card` 등 카드 규칙에서 `border` 삭제, `border-radius: var(--radius-lg)`, `padding: var(--space-3)`(모바일 포함, 600px 분기 삭제), 카드 사이 간격 `--space-3` | `static/css/app.css` |
| 주요 버튼 알약화 | `.button-primary`(`.btn-primary`)를 `border-radius: var(--radius-full)`로, `:active`에 `transform: scale(0.98)`(reduced-motion 예외) 추가. "리뷰 쓰기"·등록·검색 실행·수정 저장 버튼에 `min-height: var(--space-7)`(56px) 클래스(`.btn-hero`) 적용 | `static/css/app.css`, `templates/layout.html`(리뷰 쓰기), `reviews/new.html`, `edit.html`, `list.html`(검색 버튼) |
| 보조 버튼 배경식으로 | `.button-secondary`(`.btn-secondary`) 테두리 삭제, 배경 `--color-surface-muted` | `static/css/app.css` |
| 제목·숫자 타이트하게 | 페이지 제목(`.page h1`/`.page-title`)과 가게 평균 별점 숫자에 `letter-spacing: var(--tracking-tight)`, `font-weight: var(--font-extrabold)` 적용 | `static/css/app.css` |
| 가게 평균 별점 강조 | `.store-rating-number`를 `--text-hero`/`--font-extrabold`/`--tracking-tight`로, 상자 패딩을 `--space-4`, `--radius-lg`로 | `static/css/app.css`, `templates/stores/detail.html` |

이 표의 항목은 CLAUDE.md 원칙에 따라 제가 직접 구현하지 않습니다. 구현 담당 에이전트에게 이 표와 관련 절 번호(1.7, 2, 3.2, 5, 7.1, 7.2, 7.5, 7.9)를 그대로 전달하면 됩니다.

## 변경 이력

| 날짜 | 변경 내용 |
|---|---|
| 2026-09-17 | 최초 작성 (모바일 우선, 따뜻한 색감, Pretendard, 8px 그리드) |
| 2026-09-17 | 사용성 개선(v2): 카드 전체 클릭, CSS 전용 별점 위젯, 별 다섯 개 표시, `.read` 실제 적용, 댓글 카드화, 가게 평균 별점 강조, 검색 폼 접기, 메타 구분자, 카테고리 배지 강조. 10절(구현 반영 필요) 추가 |
| 2026-09-17 | 시각 언어 재정비(v3, 토스·카카오·네이버 참고): 카드·머리글 테두리 제거하고 그림자·배경 대비로 전환, 카드 반경 확대(`--radius-lg`), 주요 버튼 알약 모양·핵심 CTA 56px, 제목·핵심 숫자 타이트한 굵은 타이포(`--text-hero`, `--tracking-tight`), 머리글 중복 검색 폼 제거 |
