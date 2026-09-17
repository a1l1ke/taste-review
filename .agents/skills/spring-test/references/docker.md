# docker

기준: `docs/adr/ADR-003:18-19`, `SRS-001:398-402,528-535`.

- 루트 `Dockerfile`로 멀티 스테이지 빌드한다.
- 실행 단계에는 JRE 17과 jar만 담고 root가 아닌 사용자로 실행한다.
- 포트는 `PORT` 환경 변수를 따르고 없으면 8080을 쓴다.
- 프록시 헤더를 반영해 리다이렉트가 `https`가 되게 한다.
- 헬스 체크는 `/actuator/health`(`health`만 노출)로 둔다.
