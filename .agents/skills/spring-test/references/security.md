# security

기준: `docs/adr/ADR-005:17-19`, `SRS-001:479-483`.

- 인증 없이 모든 경로를 허용하고 폼 로그인·Basic·로그아웃을 끈다.
- CSRF 보호와 기본 보안 헤더는 켜 둔다.
- 작성 비밀번호는 `PasswordEncoder`(BCrypt)로 해시·대조하고 평문을 남기지 않는다.
- 기본 사용자·생성 비밀번호가 생기지 않게 한다.
- 개념: `permitAll` + 로그인 기능 해제 + CSRF 유지.
