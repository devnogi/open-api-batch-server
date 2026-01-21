# 마비노기 경매장 거래 내역 조회 및 통계 서비스

[![codecov](https://codecov.io/gh/devnogi/open-api-batch-server/branch/dev/graph/badge.svg)](https://codecov.io/gh/devnogi/open-api-batch-server)
[![License](https://img.shields.io/github/license/devnogi/open-api-batch-server)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/devnogi/open-api-batch-server)](https://github.com/devnogi/open-api-batch-server)

마비노기 경매장 거래 내역을 수집하고 분석하여 사용자에게 시세 정보를 제공하는 마이크로서비스입니다.

<br>

## 주요 기능

### 경매장 데이터 수집
- 매 1시간마다 마비노기 경매장 거래 내역을 Nexon Open API를 통해 수집
- 약 70개의 아이템 카테고리에 대한 커서 기반 페이지네이션으로 데이터 수집
- 카테고리별 요청 간 딜레이 설정으로 API Rate Limit 준수

### 뿔피리 데이터 수집
- 5분마다 거대한 외침의 뿔피리 내역 수집
- 4개 서버 지원 (류트, 만돌린, 하프, 울프)
- 지수 백오프 기반 재시도 로직 구현

### 통계 분석
- **일간 통계**: 경매 데이터 수집 완료 시 자동 트리거 (이벤트 기반)
- **주간 통계**: 매주 월요일 04:00에 집계
- **전일 통계**: 매일 00:10에 전일 통계 확정

### 데이터 조회
- 아이템별 최저가, 최고가, 평균가, 거래량 등 시세 조회
- 서버별/전체 뿔피리 내역 조회
- 아이템 옵션 필터링 (무기 공격력, 방어구 방어력 등)

<br>

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.5.0, Spring Data JPA, QueryDSL |
| **HTTP Client** | Spring WebFlux (WebClient) |
| **Database** | MySQL 8, Flyway |
| **Test** | JUnit5, Mockito, AssertJ, Testcontainers |
| **Code Quality** | Spotless (Google Java Format AOSP), Jacoco |
| **Documentation** | Swagger, Spring REST Docs |
| **DevOps** | Docker Compose, GitHub Actions |
| **Deployment** | Oracle Cloud |

<br>

## 프로젝트 구조

```
src/main/java/until/the/eternity/
├── auctionhistory/          # 경매장 거래 내역 수집 및 검색
├── hornBugle/               # 뿔피리 내역 수집 및 검색
├── statistics/              # 일간/주간 통계 집계
├── iteminfo/                # 아이템 메타데이터
├── itemoptioninfo/          # 아이템 옵션 정보
├── metalwareinfo/           # 금속류 아이템 정보
├── auctionsearchoption/     # 검색 옵션 메타데이터
├── common/                  # 공통 유틸리티, 예외, 필터
└── config/                  # 설정 (Security, QueryDSL, Web, OpenAPI)
```

### 아키텍처 (Clean Architecture)
```
interfaces/       # Controller, DTO, External API Client
  └── rest/       # REST API 엔드포인트
  └── external/   # 외부 API 연동 (Nexon Open API)
application/      # Service, Scheduler, Event
domain/           # Entity, Repository Port, Mapper
infrastructure/   # Repository 구현체, JPA
```

<br>

## API 엔드포인트

| Endpoint | Method | 설명 |
|----------|--------|------|
| `/auction-history/search` | GET | 경매 내역 검색 (필터 및 페이징) |
| `/auction-history/{id}` | GET | 단일 거래 내역 조회 |
| `/auction-history/batch` | POST | 배치 수동 실행 |
| `/horn-bugle` | GET | 뿔피리 내역 검색 (서버별/전체) |
| `/horn-bugle/batch` | POST | 뿔피리 배치 수동 실행 |
| `/statistics/daily/items` | GET | 일간 아이템 통계 |
| `/statistics/daily/subcategories` | GET | 일간 서브카테고리 통계 |
| `/statistics/daily/top-categories` | GET | 일간 상위카테고리 통계 |
| `/statistics/weekly/items` | GET | 주간 아이템 통계 |
| `/api/item-infos` | GET | 아이템 메타데이터 |
| `/api/v1/item-option-infos` | GET | 아이템 옵션 정보 |
| `/actuator/health` | GET | 헬스체크 |
| `/swagger-ui/index.html` | - | API 문서 |

<br>

## 스케줄러

| 스케줄러 | Cron 표현식 | 설명 |
|----------|-------------|------|
| 경매 내역 수집 | `0 0 * * * *` | 매 시 정각 |
| 뿔피리 수집 | `0 */5 * * * *` | 5분마다 |
| 전일 통계 확정 | `0 10 0 * * *` | 매일 00:10 |
| 주간 통계 집계 | `5 0 4 * * MON` | 매주 월요일 04:00 |

<br>

## 환경 변수

### 필수 환경 변수
```bash
# 서버
SERVER_PORT=8080

# 데이터베이스
DB_IP=localhost
DB_PORT=3306
DB_SCHEMA=devnogi
DB_USER=username
DB_PASSWORD=password

# JWT
JWT_SECRET_KEY=your-secret-key
JWT_ACCESS_TOKEN_VALIDITY=3600000
JWT_REFRESH_TOKEN_VALIDITY=86400000

# Nexon Open API
NEXON_OPEN_API_KEY=your-api-key
```

### 선택 환경 변수
```bash
# 경매 내역 배치
AUCTION_HISTORY_CRON=0 0 * * * *
AUCTION_HISTORY_DELAY_MS=1000

# 뿔피리 배치
HORN_BUGLE_CRON=0 */5 * * * *
HORN_BUGLE_MAX_RETRIES=3
HORN_BUGLE_RETRY_DELAY_MS=2000

# 통계
STATISTICS_PREVIOUS_DAY_CRON=0 10 0 * * *
STATISTICS_WEEKLY_CRON=5 0 4 * * MON
```

<br>

## 로컬 개발 환경 (Docker)

### 1. 환경 설정

```bash
# .env.local.sample을 복사하여 .env.local 생성
cp .env.local.sample .env.local

# .env.local 파일 수정
# - NEXON_OPEN_API_KEY: Nexon Open API 키
# - DB_PASSWORD: 로컬 MySQL 비밀번호
```

### 2. Docker로 실행

```bash
# 빌드 및 실행
docker-compose -f docker-compose-local.yml --env-file .env.local up --build

# 백그라운드 실행
docker-compose -f docker-compose-local.yml --env-file .env.local up -d --build

# 로그 확인
docker-compose -f docker-compose-local.yml logs -f spring-app

# 중지
docker-compose -f docker-compose-local.yml down
```

### 3. 환경별 Docker Compose 파일

| 환경 | 파일 | 설명 |
|------|------|------|
| 로컬 개발 | `docker-compose-local.yml` | 로컬 빌드, 낮은 리소스 |
| 개발 서버 | `docker-compose-dev.yml` | 개발 환경 배포 |
| 운영 서버 | `docker-compose-prod.yml` | 운영 환경 배포 |

<br>

## 빌드 및 테스트

```bash
# 빌드
./gradlew clean build

# 테스트
./gradlew test

# 코드 포맷팅
./gradlew spotlessApply

# 테스트 커버리지 리포트
./gradlew jacocoTestReport

# REST Docs 생성
./gradlew asciidoctor

# 로컬 실행
./gradlew bootRun
```

<br>

## API 응답 형식

```json
{
  "success": true,
  "code": "string",
  "message": "string",
  "data": {},
  "timestamp": "2025-01-01T12:00:00Z"
}
```

<br>

## 개발자 문서

- **실행 방법**: [Notion - How to run](https://periwinkle-bridge-1c6.notion.site/How-to-run-2385c107dcf380f993d8e733d664caf9)
- **API 명세서**: [Notion - API 명세서](https://periwinkle-bridge-1c6.notion.site/API-2195c107dcf380f2a465f9840b5d5dbf)
- **Git branch 전략**: Git-flow ([관련 블로그](https://velog.io/@kw2577/Git-branch-%EC%A0%84%EB%9E%B5))

<br>

## 지원 아이템 카테고리

약 70개의 마비노기 아이템 카테고리 지원:
- **전투 장비**: 한손/양손 무기, 검, 도끼, 둔기, 랜스 등
- **원거리 장비**: 활, 석궁, 총, 수리검, 아틀라틀
- **마법 장비**: 실린더, 스태프, 완드, 마법서, 오브
- **방어구**: 중갑/경갑/천옷, 장갑, 신발, 모자, 방패, 로브
- **악세서리**: 얼굴 장식, 날개, 꼬리, 일반 악세서리
- **특수 장비**: 악기, 라이프 도구, 마리오네트, 에코스톤, 유물
- **소모품**: 물약, 음식, 허브, 던전 통행증, 보석, 염료
- **강화 재료**: 인챈트 스크롤, 마법 가루, 설계도, 악마 스크롤
- **서적**: 책, 페이지, 마비노벨
- **구조물**: 의자, 팜 아일랜드 아이템
