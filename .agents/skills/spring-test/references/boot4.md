# boot4

기준: `HELP.md:8-12`, `SRS-001:66-70`, `SRS-001:579-592`.

- Java 17·Boot 4.1.1·Gradle wrapper 기준을 유지한다.
- 기능별 스타터를 쓰며 전역 설정은 `application.yaml`+프로필(`local/test/prod`)로 나눈다.
- 업무 설정값은 `app.*`와 타입 있는 설정 클래스로 한곳에서 읽는다.
- `build.gradle` 변경은 사용자가 직접 하며 정확한 좌표는 Boot 4.1.1 문서에서 확인한다.
