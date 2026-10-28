# DEVNOGI Batch 조회 API K6 실행 가이드

`batch_readonly_portfolio.js`는 BATCH 서버에 존재하는 조회 전용 API 20종을 운영 도메인과 로컬 도메인에서 같은 방식으로 측정하기 위한 K6 시나리오다.

## 실행 스크립트

```bash
cd open-api-batch-server
./k6/run-batch-readonly.sh [target] [profile]
```

## Target

| target | 기본 BASE_URL | TARGET_MODE | 설명 |
|---|---|---|---|
| `local` | `http://localhost:8090` | `local-batch` | batch 서버 직접 호출 |
| `prod` | `https://www.memonogi.com` | `proxy` | 운영 사용자 경로(`/api/*`) 호출 |
| `custom` | 환경 변수 `BASE_URL` 필수 | 기본 `local-batch` | 임의 도메인/포트 |

## Profile

| profile | RATE | DURATION | VU 설정 | 용도 |
|---|---:|---|---|---|
| `smoke` | 2 req/s | 20s | pre 3, max 6 | 조회 API 20종 상태 확인 |
| `portfolio` | 4 req/s | 2m | pre 5, max 10 | 포트폴리오 제출용 기본 측정 |
| `load` | 10 req/s | 5m | pre 15, max 30 | 로컬/개발 환경 부하 확인 |

모든 값은 환경 변수로 덮어쓸 수 있다.

```bash
RATE=6 DURATION=3m ./k6/run-batch-readonly.sh local portfolio
```

실제 API 호출 없이 저장 경로와 실행 설정만 확인하려면 `DRY_RUN=1`을 사용한다.

```bash
DRY_RUN=1 ./k6/run-batch-readonly.sh prod portfolio
```

## 결과 저장 규칙

실행할 때마다 `yyyyMMdd_HHmmSS` 형식의 `RUN_ID`가 자동 생성된다.

```text
k6/results/
  local/
    20260514_081500/
      batch_readonly_local_portfolio_20260514_081500.md
      batch_readonly_local_portfolio_20260514_081500.json
      batch_readonly_local_portfolio_20260514_081500.log
      batch_readonly_local_portfolio_20260514_081500.env
  prod/
    20260514_082000/
      batch_readonly_prod_portfolio_20260514_082000.md
      batch_readonly_prod_portfolio_20260514_082000.json
      batch_readonly_prod_portfolio_20260514_082000.log
      batch_readonly_prod_portfolio_20260514_082000.env
```

| 파일 | 내용 |
|---|---|
| `.md` | 포트폴리오에 붙일 수 있는 Markdown 결과 리포트 |
| `.json` | K6 summary 원본과 endpoint별 상세 지표 |
| `.log` | K6 콘솔 출력 전체 |
| `.env` | 실행 당시 target/profile/부하 설정 |

## 자주 쓰는 명령

```bash
# 로컬 batch 서버 직접 측정
./k6/run-batch-readonly.sh local portfolio

# 운영 도메인 사용자 경로 측정
./k6/run-batch-readonly.sh prod portfolio

# 로컬 smoke 테스트
./k6/run-batch-readonly.sh local smoke

# localhost:8092 같은 임의 포트 측정
BASE_URL=http://localhost:8092 TARGET_MODE=local-batch \
  ./k6/run-batch-readonly.sh custom smoke
```

## 주의사항

- 시나리오는 GET 조회 API만 호출한다.
- 인증 필요 API, 관리자 API, batch sync/write API는 제외한다.
- 운영 도메인에서 `load` 프로필은 서비스 영향이 있을 수 있으므로 트래픽이 적은 시간대에만 사용한다.
- JMeter는 이 저장소에 별도 시나리오를 추가하지 않았다. 현재 표준 산출물은 K6 Markdown/JSON 리포트다.
