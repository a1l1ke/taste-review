# ADR-003: Render에 Docker 이미지로 배포한다

| 항목 | 내용 |
|---|---|
| 상태 | Accepted |
| 결정일 | 2026-09-17 |
| 결정자 | a1l1ke |
| 대체하는 ADR | - |
| 관련 문서 | [PRD-001](../prd/PRD-001-restaurant-review-site.md) 9절, [SRS-001](../srs/SRS-001-restaurant-review-site.md) REQ-IF-031, REQ-NF-020~026 |

## 맥락

MVP를 비용과 운영 부담 없이 가볍게 공개해야 한다.
Render는 Java를 기본 런타임으로 제공하지 않으므로, Spring Boot 앱을 실행하려면 컨테이너가 필요하다.

## 결정

우리는 저장소 루트의 `Dockerfile`로 이미지를 만들어 Render의 Docker 웹 서비스로 배포한다.
이미지는 멀티 스테이지로 빌드하고, 실행 단계에는 JRE 17과 jar만 담아 root가 아닌 사용자로 실행한다.

## 검토한 대안

### 대안 1: Spring Boot Buildpacks (`bootBuildImage`)
- 장점: Dockerfile 없이 이미지를 만든다.
- 단점: Render가 저장소에서 직접 빌드할 때 쓰기 어렵고, 이미지 레지스트리 배포 과정이 추가된다.
- 채택하지 않은 이유: Render는 저장소의 Dockerfile을 바로 빌드할 수 있어 더 간단하다.

### 대안 2: Fly.io, Railway 등 다른 PaaS
- 장점: 플랫폼별로 콜드 스타트나 리전 선택이 더 유리할 수 있다.
- 단점: 설정 방식이 다르고 무료 조건이 자주 바뀐다.
- 채택하지 않은 이유: 사용자가 Render를 배포 대상으로 정했다.

## 결과

- 좋은 점: 로컬에서 `docker build`·`docker run`으로 운영과 같은 실행 환경을 확인할 수 있고, 플랫폼을 옮기기도 쉽다.
- 감수하는 점: 무료 플랜은 유휴 시 서비스가 멈춰 첫 요청이 느리고, 메모리가 512MB라 JVM 메모리 설정이 필요하다.
- 후속 작업: 포트는 `PORT` 환경 변수를 따르고, 프록시 헤더(`X-Forwarded-*`)를 반영하며, 헬스 체크는 `/actuator/health`로 둔다. 배포 절차를 문서로 남긴다.

## 변경 이력

| 날짜 | 변경 내용 |
|---|---|
| 2026-09-17 | 최초 작성 |
