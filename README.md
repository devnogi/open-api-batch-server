# 🎮 마비노기 경매장 거래 내역 조회 및 통계 서비스 

[![codecov](https://codecov.io/gh/devnogi/open-api-batch-server/branch/dev/graph/badge.svg)](https://codecov.io/gh/devnogi/open-api-batch-server)
[![License](https://img.shields.io/github/license/devnogi/open-api-batch-server)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/devnogi/open-api-batch-server)](https://github.com/devnogi/open-api-batch-server)

마비노기 경매장 거래 내역을 수집하고 분석하여 사용자에게 시세 정보를 제공하는 마이크로서비스입니다.

<br>

### 📌 주요 기능

- **데이터 수집**: 매 1시간 0분 0초에 마비노기 경매장 거래 내역 Open API를 통해 데이터 수집
- **데이터 분석**: 수집된 데이터를 기반으로 아이템별 최저가, 최고가, 평균가, 거래량 등의 통계 산출
- **경매장 거래 내역 조회**: 사용자가 아이템의 시세를 조회하고 견적을 받을 수 있는 기능

<br>

### 🛠 기술 스택

- **Backend**: Java 21, Spring Boot, Data JPA (Hibernate)
- **Test**: JUnit5, Mockito, K6
- **Database**: MySQL 8, Redis
- **DevOps**: Docker Compose, Flyway, GitHub Actions
- **Deployment**: Oracle Cloud 
- **Document**: Swagger, Notion

<br>

### 📈 프로젝트 구조

- `auction-history/`: 경매장 거래 내역 및 통계 검색
- `nexon-open-api/`: Open API 호출 및 데이터 수집
- `statics/`: 배치 작업을 통한 통계 산출

<br>

### 💻 for developers

- **How To Run**: Notion | [프로젝트 실행 방법](https://periwinkle-bridge-1c6.notion.site/How-to-run-2385c107dcf380f993d8e733d664caf9?source=copy_link)
- **API 명세서**: Notion | [API 명세서](https://periwinkle-bridge-1c6.notion.site/API-2195c107dcf380f2a465f9840b5d5dbf?source=copy_link)
- **Git branch 전략**: Git-flow [관련 블로그](https://velog.io/@kw2577/Git-branch-%EC%A0%84%EB%9E%B5)

<br>

### 🐳 로컬 개발 환경 (Docker)

로컬에서 코드를 수정하면서 개발할 때는 Docker Hub에 푸시하지 않고 로컬 빌드로 실행할 수 있습니다.

#### 1. 환경 설정

```bash
# .env.local.sample을 복사하여 .env.local 생성
cp .env.local.sample .env.local

# .env.local 파일을 열어서 필요한 값들을 수정
# - NEXON_OPEN_API_KEY: Nexon Open API 키 입력
# - DB_PASSWORD: 로컬 MySQL 비밀번호 입력
# - 기타 필요한 설정 수정
```

#### 2. 로컬에서 Docker로 실행

```bash
# 로컬 코드를 빌드하고 Docker 컨테이너로 실행
docker-compose -f docker-compose.local.yml up --build

# 백그라운드 실행
docker-compose -f docker-compose.local.yml up -d --build

# 로그 확인
docker-compose -f docker-compose.local.yml logs -f spring-app

# 중지
docker-compose -f docker-compose.local.yml down
```

#### 3. 코드 수정 후 재실행

```bash
# 코드 수정 후 다시 빌드하여 실행
docker-compose -f docker-compose.local.yml up --build

# 또는 기존 컨테이너 정리 후 재실행
docker-compose -f docker-compose.local.yml down
docker-compose -f docker-compose.local.yml up --build
```

#### 4. 환경별 실행 방법

| 환경 | Docker Compose 파일 | 설명 |
|------|---------------------|------|
| **로컬 개발** | `docker-compose.local.yml` | 로컬 코드 빌드, 낮은 리소스 사용 |
| **개발/운영 서버** | `docker-compose.yaml` | Docker Hub 이미지 사용 |

#### 5. 참고사항

- **로컬 개발**: 코드 수정 시마다 `--build` 옵션으로 재빌드 필요
- **메모리 설정**: 로컬 환경은 메모리 사용량이 낮게 설정되어 있음 (512MB)
- **데이터베이스**: `DB_IP=host.docker.internal`로 호스트 머신의 MySQL에 접근
- **포트**: 기본 8080 포트 사용 (`.env.local`에서 변경 가능)

<br>
