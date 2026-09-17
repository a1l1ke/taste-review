# DEC-003: 스타일·배포·테스트(Role C) 구현 결정 기록

- 날짜: 2026-09-17
- 범위: `static/css/app.css`, 테스트(`contract/style/deploy` 패키지), `Dockerfile`·`.dockerignore`·`render.yaml`·`.gitignore(.env*)`,
  `docs/deploy.md`만 생성·수정. A(도메인/설정)·B(웹계층) 파일과 중복 없음
- 관련: DESIGN.md 2~9절, SRS-001 5~6절(REQ-NF-001~032, REQ-DATA-004, REQ-IF-021/030/031), ADR-002/003/007

## D1. 베이스 이미지: `eclipse-temurin:17-jre-jammy` (빌드 `17-jdk-jammy`)

- JRE 17 실행 요구(REQ-NF-020)를 직접 만족하고, `jammy` 태그 고정으로 재현 빌드를 유지한다.
- `latest` 성격의 부동 태그(`17-jre`)는 쓰지 않는다.

## D2. 실행 단계에 `curl` 1개 추가

- `HEALTHCHECK /actuator/health`를 위해 최소 패키지 `curl`만 설치한다. JDK·소스는 포함하지 않는다.
- 대안(bash `/dev/tcp`)은 가독성과 이식성이 떨어져 채택하지 않았다.

## D3. `EXPOSE` 미사용

- Render가 `PORT`를 동적으로 주입하므로 포트를 고정하지 않는다. 앱은 `PORT`(없으면 8080)를 따른다(REQ-IF-031).
- 바인딩(`server.port=${PORT:8080}`)과 프록시 헤더 반영은 A 영역 설정에서 처리한다.

## D4. 테스트 스킵 + 의존성 캐시 순서

- 이미지 빌드는 `./gradlew bootJar -x test`로 테스트를 건너뛴다(테스트는 로컬·CI, REQ-NF-022).
- `gradlew·build.gradle·gradle/` 복사 → `dependencies` → `src` 복사 → `bootJar` 순으로 의존성 레이어를 재사용한다.

## D5. 단일 jar 복사: `-plain.jar` 제외

- Boot 빌드 산출물 중 plain jar를 제외하고 실행 jar 1개만 `/app/app.jar`로 복사한다.
- `build.gradle`의 plain jar 설정을 바꾸는 대신 Dockerfile에서 걸러낸다(빌드 설정은 사용자 소유).

## D6. 비root 실행: `app` 사용자

- 실행 단계에서 `groupadd/useradd -r`로 만든 `app` 사용자로 전환하고 jar 소유권을 넘긴다(REQ-NF-021).

## D7. Neon은 pooled 주소 기본 가정

- 호스트에 `-pooler`가 붙은 pooled 주소를 기본으로 문서화한다. 배포 문서는
  `postgresql://` → `jdbc:postgresql://` + `sslmode=require` 변환을 예시로 고정한다(REQ-IF-030).

## D8. `render.yaml`: free + 비밀 `sync: false`

- 무료 플랜 기준으로 코드화하고 `DB_*` 비밀 3종은 `sync: false`로 둔다(REQ-NF-026).
- `PORT`는 Render 자동 주입이라 Blueprint에 두지 않는다.

## D9. CSS 수치 표현: `calc(간격 변수 × 배수)`

- 960·720·64·160·80 폭/높이는 새 토큰을 만들지 않고 간격 변수(`--space-1`=8 기준)의 `calc()` 배수로 표현한다.
  예: 본문 폭 `calc(var(--space-1) * 120)`, 읽기 폭 `calc(var(--space-1) * 90)`.
- 미디어 조건(`min-width: 600px/960px`)의 수치는 CSS 변수로 둘 수 없어 DESIGN 6.1 값 그대로 둔다.
- Pretendard CDN `<link>`는 공통 레이아웃(B 영역)에서 연결하며, 정확한 URL은 `app.css` 상단 주석에 적어 두었다.

## D10. 테스트 전략: Green 계약 + 파일 계약 (2026-09-17 확정)

- `contract` 패키지(MockMvc, Given-When-Then): Must FUNC(001~008/010~013)마다 정상 1 + 예외 1,
  Should인 REQ-FUNC-009도 구현이 존재해 정상 1 + 예외 1 포함, 보안 4·SEO 2·헬스 1. 총 33개, 전부 Green 확인.
  - `spring-boot-starter-security-test`가 사용자 의존성에 포함되어 POST 테스트에 `.with(csrf())`를 쓴다.
  - Boot 4.1이라 `@AutoConfigureMockMvc`는 `org.springframework.boot.webmvc.test.autoconfigure` 패키지에서 가져온다.
  - 격리는 `@Transactional` 롤백, id·해시 조회는 리포지토리 대신 `JdbcTemplate` 직접 조회로 A 변경에 덜 깨지게 했다.
- `style`·`deploy` 패키지(plain JUnit + AssertJ): 토큰·배포 파일 정적 계약 12개, Green 확인.
- CSRF 403 본문은 MockMvc에서 비어 있고 실컨테이너에서는 JSON 403이라 상태 코드만 단언한다.
  레이아웃 403 페이지 연계는 A(거부 처리) 후속 작업으로 최종 보고에 넘겼다.

## D11. 검증 중 발견 사항 (수정 없이 보고만)

- 작성 페이지 `<title>`·`h1`이 `review.form.title`(값 "제목")로 나온다. 페이지 제목 전용 키가 필요하다.
- 필터 단계 403(CSRF)은 JSON 본문이라 레이아웃 403 페이지가 나가지 않는다.
- `/error` 디스패치에서는 공통 모델(`siteName` 등)이 비어 헤더 사이트 이름이 출력되지 않는다.
- 병렬 편집 중에는 알 수 없는 경로가 500으로 응답한 적이 있다. 단독 실행에서는 404가 정상이다.
- 세부 위치·원인은 최종 보고의 리뷰 항목을 따른다.

## 검증 상태

- `./gradlew test`: `style`·`deploy` 계약 Green, `contract` Red(A/B·의존성 대기) 전망. 결과는 최종 보고에 첨부한다.
- `docker build`: 가능 시 실행, 불가 시 Dockerfile 정적 점검(멀티스테이지·USER·ENV·HEALTHCHECK·EXPOSE 없음)으로 대체한다.
