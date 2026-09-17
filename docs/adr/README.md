# ADR 목록

이 디렉터리는 taste-review의 ADR(Architecture Decision Record)을 관리한다.
새 ADR은 `/adr` 스킬로 작성하며, 형식은 `.agents/skills/adr/template.md`를 따른다.

- 파일 이름: `ADR-<세 자리 번호>-<동사로 시작하는 kebab-case-슬러그>.md`
- 상태: `Proposed` → `Accepted` / `Rejected`, 이후 `Deprecated` 또는 `Superseded by ADR-<번호>`
- `Accepted`된 결정은 고치지 않고, 새 ADR로 대체한다. 파일은 삭제하지 않는다.

| 번호 | 제목 | 상태 | 결정일 |
|---|---|---|---|
| [ADR-001](ADR-001-use-ssr-with-thymeleaf.md) | Thymeleaf SSR로 화면을 만든다 | Accepted | 2026-09-17 |
| [ADR-002](ADR-002-use-h2-locally-and-neon-postgresql-in-production.md) | 로컬·테스트는 H2, 운영은 Neon PostgreSQL을 쓴다 | Accepted | 2026-09-17 |
| [ADR-003](ADR-003-deploy-to-render-with-docker.md) | Render에 Docker 이미지로 배포한다 | Accepted | 2026-09-17 |
| [ADR-004](ADR-004-use-anonymous-password-authorship.md) | 로그인 없이 닉네임과 작성 비밀번호로 글을 관리한다 | Accepted | 2026-09-17 |
| [ADR-005](ADR-005-use-spring-security-for-csrf-and-password-hashing.md) | Spring Security는 CSRF 보호와 비밀번호 해시에만 쓴다 | Accepted | 2026-09-17 |
| [ADR-006](ADR-006-hard-delete-reviews-and-comments.md) | 리뷰와 댓글은 DB에서 실제로 삭제한다 | Accepted | 2026-09-17 |
| [ADR-007](ADR-007-externalize-configuration.md) | 설정값은 환경 변수와 `app.*` 설정 프로퍼티로 주입한다 | Accepted | 2026-09-17 |
