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
- **Database**: MySQL 8
- , Redis
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
