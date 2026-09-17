---
name: spring-test
description: Spring Boot 4.1.1·Security·JPA·Docker 문법과 MockMvc TDD·BDD를 안내한다. Boot 버전, Security 설정, JPA·Flyway, Dockerfile, 실패 테스트 우선 질문이 나오면 사용한다. 코드는 직접 작성하지 않는다.
---

# spring-test

이 스킬은 `HELP.md`·`docs/adr`·`docs/srs` 결정을 기준으로 방향과 순서만 제시한다.
소스·테스트·빌드 설정은 직접 만들거나 고치지 않는다.

## 언제 읽을 것

- `references/boot4.md`: 스타터·프로필·설정 클래스 질문 시.
- `references/security.md`: CSRF·`permitAll`·BCrypt 질문 시.
- `references/jpa.md`: 엔티티·마이그레이션·H2/Neon 질문 시.
- `references/docker.md`: `Dockerfile`·Render·헬스 체크 질문 시.
- `references/test.md`: TDD·BDD·MockMvc 순서 질문 시.

## 작업 절차

1. 관련 SRS `REQ-*`와 ADR을 먼저 확인한다.
2. Given-When-Then 실패 테스트 설계를 채팅에 먼저 제시한다.
3. 통과용 최소 구현 방향을 단계별로 안내한다.
4. 확인 절차(`./gradlew test`, 줄 길이 검사)를 함께 제시한다.
5. 되돌리기 어려운 결정이 나오면 `adr` 스킬 기록을 제안한다.

## 작성 원칙

- 근거는 `HELP.md` 공식 문서와 `docs/adr`, `SRS-001`을 우선한다.
- 예시는 최소 개념 스니펫만 쓰고 전체 코드는 쓰지 않는다.
- 정확한 아티팩트명은 Boot 4.1.1 문서에서 사용자가 최종 확인한다.
