# 배포 절차 (Neon + Render)

대상: SRS-001 REQ-IF-030/031, REQ-NF-020~026. Blueprint는 `render.yaml`, 이미지 규격은 `Dockerfile` 참조.

## 1. Neon 데이터베이스 생성

1. Neon 콘솔에서 프로젝트 생성(리전은 서비스와 가깝게) 후 데이터베이스(예: `tastereview`)를 만든다.
2. 연결 정보에서 **pooled** 연결 주소를 복사한다. 호스트에 `-pooler`가 붙은 주소가 pooled 주소다.
3. 복사한 접속 문자열 형식은 아래와 같다(값은 예시).

```text
postgresql://alex:AbC123@ep-xxx-pooler.ap-southeast-1.aws.neon.tech/tastereview?sslmode=require
```

## 2. JDBC URL 변환 (REQ-IF-030)

`DB_URL`은 JDBC 형식이어야 하며, Neon 콘솔 문자열을 그대로 넣으면 연결되지 않는다.

| 항목 | 변환 규칙 | 예시 |
|---|---|---|
| `DB_URL` | scheme을 `jdbc:postgresql://`로 바꾸고, `사용자:비밀번호@`를 제거하고, `sslmode=require`를 유지한다 | `jdbc:postgresql://ep-xxx-pooler.ap-southeast-1.aws.neon.tech/tastereview?sslmode=require` |
| `DB_USERNAME` | `@` 앞의 사용자 부분만 둔다 | `alex` |
| `DB_PASSWORD` | `:` 뒤 `@` 앞의 비밀번호만 둔다 | `AbC123` |

- SSL 없이 연결하지 않는다. `sslmode=require`가 없으면 추가한다.
- 비밀번호에 특수문자가 있으면 Neon 콘솔의 percent-encoded 값을 그대로 쓴다.

## 3. Render 웹 서비스 생성

1. Render 대시보드에서 New → Web Service → 이 저장소를 선택한다.
2. Runtime은 **Docker**를 선택한다(루트 `Dockerfile`로 빌드).
3. Health check path에 `/actuator/health`를 입력한다.
4. `render.yaml` Blueprint로 만들 경우 위 값과 4절 환경 변수가 그대로 적용되며,
   비밀 값(`DB_*`)은 `sync: false`라 Render 대시보드에 직접 입력한다.

## 4. 환경 변수

| 변수 | 값 | 비고 |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | 이미지 기본값과 동일, 명시 유지 |
| `DB_URL` | 2절의 JDBC URL | 비밀, 대시보드 입력 |
| `DB_USERNAME` | DB 사용자 | 비밀, 대시보드 입력 |
| `DB_PASSWORD` | DB 비밀번호 | 비밀, 대시보드 입력 |
| `DB_POOL_MAX_SIZE` | `5` | 기본값, 필요시 조정 |
| `PORT` | (Render가 자동 주입) | 직접 설정하지 않는다 |

## 5. 배포 확인

1. `/actuator/health`가 `{"status":"UP"}`을 반환하는지 확인한다.
2. 목록 → 작성 → 상세 → 수정 → 댓글 → 삭제 흐름을 1회씩 확인한다.
3. 리다이렉트 URL이 `https`로 만들어지는지 확인한다(프록시 헤더 반영, A 영역 설정).

## 6. 문제 해결

- 첫 요청이 느림: 무료 플랜 유휴 + Neon 컴퓨트 유휴 후 콜드 스타트다. 성능 기준에서 제외한다(SRS 2.5절).
- DB 연결 실패: `DB_URL`이 `jdbc:postgresql://`로 시작하는지, pooled 호스트인지, `sslmode=require`가 있는지 확인한다.
- `pg_trgm` 확장 실패: Neon SQL Editor에서 수동 생성 후 재배포한다(DEC-001 D10).

## 7. 운영 연결 수동 확인 기록 (REQ-NF-032)

배포 전 Neon 개발 브랜치에 `prod` 프로필로 연결해 마이그레이션과 검색 동작을 확인하고 아래에 기록한다.

- 확인일:
- Neon 브랜치:
- 마이그레이션 결과:
- 검색 확인(키워드/지역/카테고리/별점):
