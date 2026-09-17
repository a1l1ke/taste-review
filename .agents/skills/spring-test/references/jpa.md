# jpa

기준: `docs/adr/ADR-002:18-19`, `SRS-001:407-411,457-462,473-474`.

- 스키마는 Flyway로만 만들고 바꾸며 공통 SQL은 양 DB에서 실행되게 쓴다.
- `prod`는 스키마 자동 생성 대신 검증만 쓴다.
- 뷰 렌더링 중 지연 로딩을 막도록 관련 설정을 끈다.
- `local/test`는 H2 인메모리, `prod`는 Neon PostgreSQL을 쓴다.
- 개념: 마이그레이션 우선, DB 전용 SQL은 `{vendor}` 경로로 분리.
