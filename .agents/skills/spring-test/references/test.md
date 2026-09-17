# test

기준: `SRS-001:540-543`, `SRS-001:594-607`, `ADR-005:36-37`.

순서: 실패 테스트(Red) → 최소 구현(Green) → 정리(Refactor).

1. SRS `REQ-*` 하나를 고르고 Given-When-Then으로 실패 테스트를 먼저 설계한다.
2. 정상 흐름 1개 이상, 예외 흐름(검증 실패·비밀번호 불일치·404·CSRF 403) 1개 이상을 둔다.
3. 외부 DB·네트워크 없이 H2만으로 `./gradlew test`가 통과해야 한다.
4. 단계마다 `./gradlew test`로 확인하고 `Should/Could`는 시간이 모자라면 미룬다.
