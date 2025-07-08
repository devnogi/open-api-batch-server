# 🎮 마비노기 경매장 거래 내역 조회 및 통계 서비스 

![Coverage](https://github.com/devnogi/open-api-batch-server/dev/badges/coverage.svg)
[![License](https://img.shields.io/github/license/devnogi/open-api-batch-server)](LICENSE)
[![Last Commit](https://img.shields.io/github/last-commit/devnogi/open-api-batch-server)](https://github.com/devnogi/open-api-batch-server)

마비노기 경매장 거래 내역을 수집하고 분석하여 사용자에게 시세 정보를 제공하는 마이크로서비스입니다.

<br>

### 📌 주요 기능

- **데이터 수집**: 1시간 간격으로 게임 경매장 거래 내역을 Open API를 통해 수집
- **데이터 분석**: 수집된 데이터를 기반으로 시세 변동, 거래량 등의 통계 정보 제공
- **아이템 시세 견적**: 사용자가 아이템의 시세를 조회하고 견적을 받을 수 있는 기능

<br>

### 🛠 기술 스택

- **Backend**: Spring Boot, Data JPA (Hibernate)
- **Test**: JUnit5, Mockito, K6
- **Database**: MySQL, Redis
- **DevOps**: Docker Compose, Flyway, GitHub Actions
- **Deployment**: Oracle Cloud 
- **Document**: Spring REST Docs, Swagger

<br>

### 📈 프로젝트 구조

- `api/`: Open API 연동 및 데이터 수집
- `batch/`: 배치 작업을 통한 데이터 수집 및 처리
- `estimate/`: 아이템 시세 견적 기능

<br>

### 💻 for developers

- **Git branch 전략**: Git-flow [관련 블로그](https://velog.io/@kw2577/Git-branch-%EC%A0%84%EB%9E%B5)

<br>
