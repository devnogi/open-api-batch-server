# CD Pipeline 완전 가이드

**작성일**: 2025-10-31
**대상**: open-api-batch-server
**목적**: GitHub Actions, Dockerfile, docker-compose를 활용한 CD 파이프라인 전체 흐름 이해

---

## 📖 목차

1. [CD 파이프라인 개요](#cd-파이프라인-개요)
2. [전체 아키텍처](#전체-아키텍처)
3. [GitHub Actions Workflow 상세](#github-actions-workflow-상세)
4. [Dockerfile 상세](#dockerfile-상세)
5. [docker-compose.yaml 상세](#docker-composeyaml-상세)
6. [환경 변수 (.env) 상세](#환경-변수-env-상세)
7. [GitHub Secrets 설정 가이드](#github-secrets-설정-가이드)
8. [CD 파이프라인 실행 흐름](#cd-파이프라인-실행-흐름)
9. [트러블슈팅](#트러블슈팅)

---

## CD 파이프라인 개요

### 🎯 목표
- **개발 서버**: `dev` 브랜치 푸시 시 자동 배포
- **운영 서버**: `main` 브랜치 푸시 시 자동 배포
- 각 환경별로 독립적인 설정 사용

### 🏗️ 구성 요소

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. GitHub Actions                                               │
│    - push-cd-dev.yml (dev 브랜치)                               │
│    - push-cd-prod.yml (main 브랜치)                             │
├─────────────────────────────────────────────────────────────────┤
│ 2. Dockerfile                                                   │
│    - Multi-stage build                                          │
│    - Gradle 빌드 → JAR 추출 → Runtime 이미지                    │
├─────────────────────────────────────────────────────────────────┤
│ 3. docker-compose.yaml                                          │
│    - 컨테이너 정의 (spring-app)                                 │
│    - 오토힐 컨테이너 (autoheal)                                 │
│    - 환경 변수 주입                                             │
├─────────────────────────────────────────────────────────────────┤
│ 4. .env 파일                                                    │
│    - .env.dev (개발 환경 설정)                                  │
│    - .env.prod (운영 환경 설정)                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 전체 아키텍처

### 개발 환경 (Dev) 배포 플로우

```mermaid
graph TB
    A[Developer pushes to dev branch] --> B[GitHub Actions Triggered]
    B --> C[CI Stage: Run Tests]
    C --> D[Build Docker Image with Dockerfile]
    D --> E[Push to Docker Hub with 'dev' tag]
    E --> F[SSH to Dev Server]
    F --> G[Write .env.dev content to .env]
    G --> H[Copy docker-compose.yaml to server]
    H --> I[docker compose down]
    I --> J[docker compose up -d]
    J --> K[Health Check: Container Running]
    K --> L[Health Check: Docker Health Status]
    L --> M[Health Check: Actuator Endpoint]
    M --> N[Deployment Complete ✅]
```

### 운영 환경 (Prod) 배포 플로우

```mermaid
graph TB
    A[Developer pushes to main branch] --> B[GitHub Actions Triggered]
    B --> C[CI Stage: Run Tests Strictly]
    C --> D[Build Docker Image with Dockerfile]
    D --> E[Push to Docker Hub with 'prod' and 'latest' tags]
    E --> F[SSH to Prod Server]
    F --> G[Write .env.prod content to .env]
    G --> H[Copy docker-compose.yaml to server]
    H --> I[docker compose down]
    I --> J[docker compose up -d]
    J --> K[Health Check: Container Running]
    K --> L[Health Check: Docker Health Status]
    L --> M[Health Check: Actuator Endpoint]
    M --> N[Smoke Test: HTTP 200 Check]
    N --> O[Deployment Complete ✅]
```

---

## GitHub Actions Workflow 상세

### 파일 구조

```
.github/workflows/
├── pr-ci.yml           # PR 시 테스트
├── push-cd-dev.yml     # dev 브랜치 배포
└── push-cd-prod.yml    # main 브랜치 배포
```

### 1. push-cd-dev.yml 상세 분석

**파일**: `.github/workflows/push-cd-dev.yml`

#### 트리거 조건

```yaml
on:
  push:
    branches: [ dev ]  # dev 브랜치에만 반응
```

#### 전체 Job 구조

```yaml
jobs:
  ci-cd-dev:
    name: Test, Build, and Deploy to Dev Server
    runs-on: ubuntu-latest  # GitHub의 Ubuntu 러너 사용
```

---

#### 단계별 상세 설명

##### **Stage 1: CI (테스트 & 커버리지)**

**Step 1-1: 코드 체크아웃**
```yaml
- name: Checkout code
  uses: actions/checkout@v4
```
- GitHub 저장소의 코드를 러너로 가져옴
- 기본적으로 현재 브랜치 (dev) 체크아웃

**Step 1-2: JDK 21 설정**
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: 'gradle'
```
- Eclipse Temurin JDK 21 설치
- `cache: 'gradle'` → Gradle 의존성 자동 캐싱

**Step 1-3: Gradle 캐시**
```yaml
- name: Cache Gradle packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```
- Gradle 의존성을 캐시하여 빌드 속도 향상
- `key`는 OS와 Gradle 파일 해시로 생성
- 의존성 변경 시에만 새로 다운로드

**Step 1-4: 테스트 실행**
```yaml
- name: Run Tests and Generate Coverage
  run: ./gradlew clean test jacocoTestReport --no-daemon
```
- 모든 테스트 실행
- JaCoCo를 통해 커버리지 리포트 생성
- `--no-daemon` → 메모리 절약

**Step 1-5: Codecov 업로드**
```yaml
- name: Upload JaCoCo report to Codecov
  uses: codecov/codecov-action@v4
  with:
    token: ${{ secrets.CODECOV_TOKEN }}
    files: build/reports/jacoco/test/jacocoTestReport.xml
    fail_ci_if_error: false  # Dev에서는 실패해도 계속 진행
    flags: dev
```
- 코드 커버리지를 Codecov 서비스에 업로드
- `flags: dev` → 환경 구분

---

##### **Stage 2: CD (Docker 빌드 & 푸시)**

**Step 2-1: Docker Buildx 설정**
```yaml
- name: Set up Docker Buildx
  uses: docker/setup-buildx-action@v3
```
- Docker Buildx 활성화 (multi-platform 빌드, 캐싱 지원)
- GitHub Actions 캐시와 연동 가능

**Step 2-2: Docker Hub 로그인**
```yaml
- name: Log in to Docker Hub
  uses: docker/login-action@v3
  with:
    username: ${{ secrets.DOCKER_USERNAME }}
    password: ${{ secrets.DOCKER_PASSWORD }}
```
- Docker Hub에 로그인
- 이미지 푸시 권한 획득

**Step 2-3: Docker 이미지 빌드 & 푸시**
```yaml
- name: Build and push Docker image
  uses: docker/build-push-action@v5
  with:
    context: .                    # Dockerfile이 있는 디렉토리
    push: true                    # 빌드 후 자동 푸시
    tags: |
      ${{ secrets.DOCKER_USERNAME }}/${{ secrets.DOCKER_REPO }}:dev
      ${{ secrets.DOCKER_USERNAME }}/${{ secrets.DOCKER_REPO }}:dev-${{ github.sha }}
    cache-from: type=gha          # GitHub Actions 캐시에서 읽기
    cache-to: type=gha,mode=max   # GitHub Actions 캐시에 쓰기
    build-args: |
      BUILDKIT_INLINE_CACHE=1
```

**생성되는 태그**:
- `username/repo:dev` → 최신 dev 이미지
- `username/repo:dev-abc1234` → 커밋 SHA로 버전 추적

**캐싱 메커니즘**:
```
첫 빌드:
  1. Dockerfile의 모든 단계 실행
  2. 각 레이어를 GitHub Actions 캐시에 저장

두 번째 빌드 이후:
  1. 변경되지 않은 레이어는 캐시에서 가져옴
  2. 변경된 레이어만 다시 빌드
  3. 빌드 시간 대폭 단축
```

---

##### **Stage 3: 배포 (SSH & Deploy)**

**Step 3-1: SSH 키 설정**
```yaml
- name: Setup SSH key and config
  run: |
    mkdir -p ~/.ssh
    echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/my-key.pem
    chmod 400 ~/.ssh/my-key.pem
    ssh-keyscan -H ${{ secrets.SERVER_HOST }} >> ~/.ssh/known_hosts
    echo -e "Host *\n  ServerAliveInterval 60\n  ServerAliveCountMax 3" >> ~/.ssh/config
```

**각 명령 설명**:
1. `mkdir -p ~/.ssh` → SSH 디렉토리 생성
2. `echo "${{ secrets.SSH_PRIVATE_KEY }}" > ~/.ssh/my-key.pem` → 비밀키 저장
3. `chmod 400 ~/.ssh/my-key.pem` → 권한 설정 (읽기 전용)
4. `ssh-keyscan -H ${{ secrets.SERVER_HOST }} >> ~/.ssh/known_hosts` → 호스트 키 등록 (첫 연결 시 프롬프트 방지)
5. `ServerAliveInterval 60` → 60초마다 keep-alive 신호 전송

**Step 3-2: 서버 디렉토리 생성**
```yaml
- name: Create app directory on server
  run: |
    ssh -i ~/.ssh/my-key.pem ${{ secrets.SERVER_USER }}@${{ secrets.SERVER_HOST }} "mkdir -p /home/${{ secrets.SERVER_USER }}/app/logs"
```
- 서버에 애플리케이션 디렉토리 생성
- `logs` 디렉토리도 함께 생성 (docker-compose에서 마운트)

**Step 3-3: docker-compose.yaml 복사**
```yaml
- name: Copy docker-compose.yaml to server
  run: |
    scp -i ~/.ssh/my-key.pem docker-compose.yaml ${{ secrets.SERVER_USER }}@${{ secrets.SERVER_HOST }}:/home/${{ secrets.SERVER_USER }}/app/
```
- 로컬의 `docker-compose.yaml`을 서버로 복사
- SCP (Secure Copy Protocol) 사용

**Step 3-4: 배포 실행**
```yaml
- name: Deploy to Dev Server
  run: |
    ssh -i ~/.ssh/my-key.pem ${{ secrets.SERVER_USER }}@${{ secrets.SERVER_HOST }} << 'EOF'
      cd /home/${{ secrets.SERVER_USER }}/app

      # .env.dev 내용을 .env로 저장
      echo "${{ secrets.ENV_FILE_DEV }}" > .env

      # 최신 dev 이미지 pull
      docker pull ${{ secrets.DOCKER_USERNAME }}/${{ secrets.DOCKER_REPO }}:dev

      # 기존 컨테이너 중지 및 제거
      docker compose down

      # 새 컨테이너 시작
      docker compose up -d

      echo "✅ Dev deployment complete"
    EOF
```

**서버에서 실행되는 순서**:
1. 앱 디렉토리로 이동
2. `.env.dev` 내용을 `.env` 파일로 저장
3. Docker Hub에서 최신 `dev` 태그 이미지 다운로드
4. 기존 컨테이너 중지 및 삭제
5. 새 컨테이너를 백그라운드로 시작 (`-d`)

---

##### **Stage 4: Health Check**

```yaml
- name: Comprehensive Health Check
  run: |
    ssh -i ~/.ssh/my-key.pem ${{ secrets.SERVER_USER }}@${{ secrets.SERVER_HOST }} << 'EOF'
      echo "=== Starting Health Check ==="

      # 1. 컨테이너 실행 확인
      CONTAINER_ID=$(docker ps -q --filter "name=spring-app")
      if [ -z "$CONTAINER_ID" ]; then
        echo "❌ Container not running"
        docker ps -a
        docker logs spring-app --tail 50
        exit 1
      fi
      echo "✅ Container is running (ID: $CONTAINER_ID)"

      # 2. Docker health check 상태 대기
      echo "Waiting for container to become healthy..."
      for i in {1..30}; do
        HEALTH_STATUS=$(docker inspect --format='{{.State.Health.Status}}' spring-app 2>/dev/null || echo "no-healthcheck")

        if [ "$HEALTH_STATUS" == "healthy" ]; then
          echo "✅ Container is healthy"
          break
        elif [ "$HEALTH_STATUS" == "no-healthcheck" ]; then
          echo "⚠️  No healthcheck configured, checking actuator directly"
          break
        fi

        echo "Current health status: $HEALTH_STATUS ($i/30)"
        sleep 10

        if [ $i -eq 30 ]; then
          echo "❌ Container failed to become healthy after 5 minutes"
          docker logs spring-app --tail 100
          exit 1
        fi
      done

      # 3. Spring Boot actuator health endpoint 확인
      echo "Checking actuator health endpoint..."
      for i in {1..20}; do
        HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health || echo "")

        if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
          echo "✅ Application health check passed"
          echo "Health response: $HEALTH_RESPONSE"
          break
        fi

        echo "Waiting for application to start... ($i/20)"
        sleep 10

        if [ $i -eq 20 ]; then
          echo "❌ Application health check failed after 3+ minutes"
          echo "Last response: $HEALTH_RESPONSE"
          docker logs spring-app --tail 100
          exit 1
        fi
      done

      echo "=== Health Check Complete ==="
      docker ps --filter "name=spring-app"
    EOF
```

**3단계 Health Check**:

1. **컨테이너 실행 확인**
   - `docker ps`로 `spring-app` 컨테이너 확인
   - 없으면 즉시 실패 및 로그 출력

2. **Docker Health Status 대기** (최대 5분)
   - `docker-compose.yaml`에 정의된 healthcheck 사용
   - `wget http://localhost:8080/actuator/health` 실행
   - `healthy` 상태가 될 때까지 10초마다 체크

3. **Actuator Health Endpoint 확인** (최대 3분)
   - `curl http://localhost:8080/actuator/health` 직접 호출
   - JSON 응답에서 `"status":"UP"` 확인
   - 애플리케이션이 완전히 시작되었는지 검증

**실패 시 동작**:
- 각 단계 실패 시 `exit 1`로 워크플로우 중단
- 로그 출력 (`docker logs spring-app --tail 100`)
- GitHub Actions에서 빨간색 실패 표시

---

### 2. push-cd-prod.yml 차이점

**파일**: `.github/workflows/push-cd-prod.yml`

#### Dev와의 주요 차이

| 항목 | Dev | Prod |
|------|-----|------|
| **트리거 브랜치** | `dev` | `main` |
| **Docker 태그** | `dev`, `dev-{sha}` | `prod`, `latest`, `prod-{sha}` |
| **환경 변수** | `ENV_FILE_DEV` | `ENV_FILE_PROD` |
| **SSH 키** | `SSH_PRIVATE_KEY` | `PROD_SSH_PRIVATE_KEY` |
| **서버 정보** | `SERVER_HOST`, `SERVER_USER` | `PROD_SERVER_HOST`, `PROD_SERVER_USER` |
| **테스트 실패 처리** | `fail_ci_if_error: false` | `fail_ci_if_error: true` ⚠️ |
| **Health Check 시간** | 5분 + 3분 | 6분 + 5분 (더 엄격) |
| **Smoke Test** | ❌ | ✅ (HTTP 200 확인) |

#### Prod만의 추가 검증

```yaml
# 4. Smoke test: Check if API responds
echo "Running smoke test..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
if [ "$HTTP_CODE" == "200" ]; then
  echo "✅ Smoke test passed (HTTP $HTTP_CODE)"
else
  echo "❌ Smoke test failed (HTTP $HTTP_CODE)"
  exit 1
fi
```

**실패 시 롤백 가이드**:
```yaml
- name: Rollback on failure
  if: failure()
  run: |
    echo "❌ Deployment failed! Consider manual rollback if needed."
    echo "To rollback, SSH to server and run:"
    echo "  docker pull ${{ secrets.DOCKER_USERNAME }}/${{ secrets.DOCKER_REPO }}:prod-<previous-sha>"
    echo "  docker compose down && docker compose up -d"
```

---

## Dockerfile 상세

**파일**: `Dockerfile`

### Multi-Stage Build 구조

```dockerfile
# Stage 1: Builder
FROM gradle:8.5-jdk21-alpine AS builder

# Stage 2: Extractor
FROM eclipse-temurin:21-jre-alpine AS extractor

# Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
```

---

### Stage 1: Builder (애플리케이션 빌드)

```dockerfile
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app

# Gradle 의존성 다운로드 (캐시 최적화)
COPY ../gradle gradle
COPY ../gradlew .
COPY ../gradle.properties .
COPY ../settings.gradle.kts .
COPY ../build.gradle.kts .

RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY ../src src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle clean bootJar -x test --no-daemon

# JAR 파일 이름 변경
RUN mkdir -p /app/build/extracted && \
    cp /app/build/libs/*.jar /app/build/app.jar
```

**왜 의존성을 먼저 복사하나요?**
```
Docker Layer 캐싱 최적화:
1. build.gradle.kts 변경 없음 → 의존성 다운로드 스킵
2. 소스 코드만 변경 → 의존성은 캐시 사용, 빌드만 실행
3. 빌드 시간 대폭 단축
```

**출력물**:
- `/app/build/app.jar` → Spring Boot 실행 가능한 JAR 파일

---

### Stage 2: Extractor (Spring Boot Layer 추출)

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/app.jar app.jar

# Spring Boot Layered JAR 추출
RUN java -Djarmode=layertools -jar app.jar extract
```

**Spring Boot Layered JAR**:
```
app.jar 내부 구조:
├── dependencies/          → 외부 라이브러리 (변경 적음)
├── spring-boot-loader/    → Spring Boot 로더 (변경 거의 없음)
├── snapshot-dependencies/ → SNAPSHOT 의존성 (가끔 변경)
└── application/           → 애플리케이션 코드 (자주 변경)
```

**왜 레이어를 분리하나요?**
```
Docker Layer 캐싱 효과:
1. 코드만 변경 → application/ 레이어만 재빌드
2. 의존성 변경 안 함 → dependencies/ 캐시 사용
3. 이미지 빌드 속도 향상, 이미지 크기 최적화
```

---

### Stage 3: Runtime (최종 실행 이미지)

```dockerfile
FROM eclipse-temurin:21-jre-alpine

# 메타데이터
LABEL maintainer="DevNogi Team"
LABEL description="Open API Batch Server - Mabinogi Auction Data Collector"
LABEL version="0.0.1"

# 보안: non-root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 레이어별로 복사 (의존성 변경 시 캐시 활용)
COPY --from=extractor --chown=spring:spring /app/dependencies/ ./
COPY --from=extractor --chown=spring:spring /app/spring-boot-loader/ ./
COPY --from=extractor --chown=spring:spring /app/snapshot-dependencies/ ./
COPY --from=extractor --chown=spring:spring /app/application/ ./

# 사용자 전환
USER spring:spring

# JVM 메모리 설정 (기본값)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8092

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

**보안 설정**:
- `non-root` 사용자로 실행 → 컨테이너 탈출 공격 방어
- `--chown=spring:spring` → 파일 소유권 설정

**환경 변수 우선순위**:
```
1. docker-compose.yaml의 JAVA_OPTS (최우선)
2. Dockerfile의 ENV JAVA_OPTS (기본값)
```

**실행 명령**:
```bash
java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher
```
- `$JAVA_OPTS` → JVM 옵션 적용
- `JarLauncher` → Spring Boot의 커스텀 클래스 로더 사용

---

### Multi-Stage Build의 장점

| 항목 | Single-Stage | Multi-Stage |
|------|--------------|-------------|
| **이미지 크기** | ~800MB | ~250MB |
| **빌드 도구 포함** | ✅ (Gradle 포함) | ❌ (JRE만) |
| **캐싱 효율** | 낮음 | 높음 |
| **보안** | 낮음 (빌드 도구 노출) | 높음 |

**빌드 과정 요약**:
```
1. Builder Stage → JAR 파일 생성
2. Extractor Stage → JAR을 레이어로 분리
3. Runtime Stage → 레이어를 순서대로 복사 + 실행
```

---

## docker-compose.yaml 상세

**파일**: `docker-compose.yaml`

### 전체 구조

```yaml
version: "3.8"

services:
  spring-app:      # 메인 애플리케이션
    # ... 설정 ...

  autoheal:        # 자동 재시작 관리
    # ... 설정 ...

networks:
  app-network:     # 컨테이너 간 통신
    driver: bridge
```

---

### Service 1: spring-app (메인 애플리케이션)

#### 이미지 및 컨테이너 설정

```yaml
spring-app:
  image: ${DOCKER_USERNAME}/${DOCKER_REPO}:${DOCKER_IMAGE_TAG:-latest}
  container_name: spring-app
  ports:
    - "${SERVER_PORT}:${SERVER_PORT}"
  env_file:
    - .env
```

**이미지 태그 결정**:
```
.env.dev → DOCKER_IMAGE_TAG=dev
  → username/repo:dev

.env.prod → DOCKER_IMAGE_TAG=prod
  → username/repo:prod

.env 없음 → 기본값 latest
  → username/repo:latest
```

**포트 매핑**:
```
${SERVER_PORT}:${SERVER_PORT}
     ↓              ↓
  호스트 포트    컨테이너 포트

예: 8080:8080
→ 호스트의 8080 → 컨테이너의 8080
```

---

#### 환경 변수 설정

```yaml
labels:
  autoheal: "true"  # autoheal 서비스가 이 컨테이너를 관리

environment:
  # Spring 설정
  SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE}
  LANG: C.UTF-8
  LC_ALL: C.UTF-8
  SERVER_PORT: ${SERVER_PORT}

  # Database
  DB_IP: ${DB_IP}
  DB_PORT: ${DB_PORT}
  DB_SCHEMA: ${DB_SCHEMA}
  DB_USER: ${DB_USER}
  DB_PASSWORD: ${DB_PASSWORD}

  # Security
  JWT_SECRET_KEY: ${JWT_SECRET_KEY}
  JWT_ACCESS_TOKEN_VALIDITY: ${JWT_ACCESS_TOKEN_VALIDITY}
  JWT_REFRESH_TOKEN_VALIDITY: ${JWT_REFRESH_TOKEN_VALIDITY}

  # External API
  NEXON_OPEN_API_KEY: ${NEXON_OPEN_API_KEY}
  AUCTION_HISTORY_DELAY_MS: ${AUCTION_HISTORY_DELAY_MS}
  AUCTION_HISTORY_CRON: "${AUCTION_HISTORY_CRON}"
  AUCTION_HISTORY_MIN_PRICE_CRON: "${AUCTION_HISTORY_MIN_PRICE_CRON}"

  # JVM Configuration
  JAVA_OPTS: >-
    -Xms${JAVA_OPTS_XMS}
    -Xmx${JAVA_OPTS_XMX}
    -XX:MaxMetaspaceSize=${JAVA_OPTS_MAX_METASPACE_SIZE}
    -XX:ReservedCodeCacheSize=${JAVA_OPTS_RESERVED_CODE_CACHE_SIZE}
    -XX:MaxDirectMemorySize=${JAVA_OPTS_MAX_DIRECT_MEMORY_SIZE}
    -Xss${JAVA_OPTS_XSS}
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=${JAVA_OPTS_MAX_GC_PAUSE_MILLIS}
    -XX:G1HeapRegionSize=${JAVA_OPTS_G1_HEAP_REGION_SIZE}
    -XX:InitiatingHeapOccupancyPercent=${JAVA_OPTS_INITIATING_HEAP_OCCUPANCY_PERCENT}
    -XX:+TieredCompilation
    -XX:TieredStopAtLevel=${JAVA_OPTS_TIERED_STOP_AT_LEVEL}
    -XX:CICompilerCount=${JAVA_OPTS_CI_COMPILER_COUNT}
    -XX:+UseCompressedOops
    -XX:+UseCompressedClassPointers
    -Djava.security.egd=file:/dev/./urandom
    -Dspring.jmx.enabled=false
```

**JAVA_OPTS가 Dockerfile보다 우선**:
```
Dockerfile: ENV JAVA_OPTS="-Xms512m -Xmx1024m ..."
docker-compose: JAVA_OPTS: "-Xms256m -Xmx512m ..."
                              ↑ 이것이 적용됨
```

---

#### 볼륨 마운트

```yaml
volumes:
  - ./logs:/app/logs
  # - ./config:/app/config:ro  # Optional
```

**로그 디렉토리**:
```
호스트: ./logs
  ↓ 마운트
컨테이너: /app/logs

→ 애플리케이션 로그가 호스트에 저장됨
→ 컨테이너 삭제해도 로그 유지
```

---

#### 재시작 정책

```yaml
restart: on-failure:${RESTART_POLICY_MAX_RETRIES}
```

**동작 방식**:
```
컨테이너 exit code != 0 → 재시작 시도
  ↓
최대 ${RESTART_POLICY_MAX_RETRIES}번까지 재시도
  ↓
그래도 실패 → 중지 (autoheal이 개입)
```

**.env.dev**: `RESTART_POLICY_MAX_RETRIES=5`
**.env.prod**: `RESTART_POLICY_MAX_RETRIES=10`

---

#### 리소스 제한

```yaml
deploy:
  resources:
    limits:
      memory: ${DOCKER_MEMORY_LIMIT}       # Hard limit
    reservations:
      memory: ${DOCKER_MEMORY_RESERVATION} # Soft limit
```

**메모리 제한 동작**:
```
Reservation (예약): 512M
  → 컨테이너가 최소 512M 보장받음

Limit (제한): 750M
  → 컨테이너가 750M 이상 사용 불가
  → 초과 시 OOM Killer가 프로세스 종료
```

**환경별 설정**:
| 환경 | Reservation | Limit |
|------|-------------|-------|
| Local | 384M | 500M |
| Dev | 512M | 750M |
| Prod | 2G | 3G |

---

#### Health Check

```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:${SERVER_PORT}/actuator/health"]
  interval: ${HEALTHCHECK_INTERVAL}
  timeout: ${HEALTHCHECK_TIMEOUT}
  retries: ${HEALTHCHECK_RETRIES}
  start_period: ${HEALTHCHECK_START_PERIOD}
```

**각 파라미터 설명**:

| 파라미터 | 설명 | Dev 값 | Prod 값 |
|----------|------|---------|---------|
| `interval` | 체크 주기 | 30s | 20s |
| `timeout` | 응답 대기 시간 | 15s | 10s |
| `retries` | 연속 실패 횟수 | 4 | 5 |
| `start_period` | 시작 유예 기간 | 120s | 180s |

**Health Check 상태 변화**:
```
1. starting (start_period 동안)
   → Health check 실패해도 카운트 안 됨

2. healthy
   → test 명령이 성공 (exit 0)

3. unhealthy
   → retries번 연속 실패
   → autoheal이 컨테이너 재시작
```

**테스트 명령 분석**:
```bash
wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health

--no-verbose: 출력 최소화
--tries=1: 1번만 시도
--spider: 파일 다운로드 안 함, 존재 여부만 확인
```

---

#### 로깅 설정

```yaml
logging:
  driver: "json-file"
  options:
    max-size: ${LOGGING_MAX_SIZE}
    max-file: "${LOGGING_MAX_FILE}"
```

**로그 로테이션**:
```
Dev:
  max-size: 10m → 로그 파일 10MB 도달 시 로테이션
  max-file: 3   → 최대 3개 파일 유지

→ 총 로그 크기: 최대 30MB

Prod:
  max-size: 50m
  max-file: 10
→ 총 로그 크기: 최대 500MB
```

**로그 파일 경로**:
```
/var/lib/docker/containers/<container-id>/<container-id>-json.log
/var/lib/docker/containers/<container-id>/<container-id>-json.log.1
/var/lib/docker/containers/<container-id>/<container-id>-json.log.2
```

---

### Service 2: autoheal (자동 재시작)

```yaml
autoheal:
  image: willfarrell/autoheal:latest
  container_name: autoheal
  restart: unless-stopped
  environment:
    AUTOHEAL_INTERVAL: ${AUTOHEAL_INTERVAL}
    AUTOHEAL_START_PERIOD: ${AUTOHEAL_START_PERIOD}
    AUTOHEAL_DEFAULT_STOP_TIMEOUT: ${AUTOHEAL_DEFAULT_STOP_TIMEOUT}
    DOCKER_SOCK: /var/run/docker.sock
  volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro
  networks:
    - app-network
  deploy:
    resources:
      limits:
        memory: ${AUTOHEAL_MEMORY_LIMIT}
      reservations:
        memory: ${AUTOHEAL_MEMORY_RESERVATION}
```

**autoheal의 역할**:
```
1. AUTOHEAL_INTERVAL마다 모든 컨테이너 검사
2. label "autoheal=true"인 컨테이너 중 unhealthy 상태 발견
3. 해당 컨테이너 재시작
4. Graceful shutdown 대기 (AUTOHEAL_DEFAULT_STOP_TIMEOUT)
5. 시간 초과 시 강제 종료 (docker kill)
```

**Docker 소켓 마운트**:
```yaml
volumes:
  - /var/run/docker.sock:/var/run/docker.sock:ro
```
- Docker daemon과 통신하기 위한 소켓
- `:ro` (read-only) → 보안 강화
- autoheal이 다른 컨테이너를 제어할 수 있게 함

**동작 시나리오**:
```
1. spring-app이 OOM으로 비정상 종료
2. restart: on-failure:5로 5번 재시도
3. 5번 모두 실패 → 컨테이너 중지
4. Health check 실패 → unhealthy 상태
5. autoheal이 30초마다 체크
6. unhealthy 발견 → spring-app 재시작
7. Health check 통과 → healthy 상태
```

---

## 환경 변수 (.env) 상세

### .env 파일 구조

```bash
# 각 환경별 파일
.env   # 로컬 개발
.env.dev     # 개발 서버
.env.prod    # 운영 서버

# 실제 사용
.env         # 현재 활성화된 환경
```

### 주요 환경 변수 카테고리

#### 1. Application Configuration
```bash
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=default  # or prod
```

#### 2. Database Configuration
```bash
DB_IP=localhost              # Dev: dev-db-ip, Prod: prod-db-ip
DB_PORT=3306
DB_SCHEMA=devnogi_local      # Dev: devnogi_dev, Prod: devnogi_prod
DB_USER=root                 # Dev/Prod: devnogi_user
DB_PASSWORD=your_password    # 환경별로 다른 비밀번호!
```

#### 3. Security Configuration
```bash
JWT_SECRET_KEY=your_secret_key  # 환경별로 다른 키! (최소 32자)
JWT_ACCESS_TOKEN_VALIDITY=3600   # 1 hour
JWT_REFRESH_TOKEN_VALIDITY=86400 # 1 day
```

#### 4. External API Configuration
```bash
NEXON_OPEN_API_KEY=your_api_key
AUCTION_HISTORY_DELAY_MS=300          # Local: 500, Dev: 300, Prod: 200
AUCTION_HISTORY_CRON=0 0 * * * *
AUCTION_HISTORY_MIN_PRICE_CRON=0 30 * * * *
```

#### 5. Docker Configuration
```bash
DOCKER_USERNAME=your_username
DOCKER_PASSWORD=your_password
DOCKER_REPO=open-api-batch-server
DOCKER_IMAGE_TAG=dev              # Local: latest, Dev: dev, Prod: prod
```

#### 6. JVM Memory Configuration
```bash
# Heap
JAVA_OPTS_XMS=256m               # Local: 128m, Dev: 256m, Prod: 512m
JAVA_OPTS_XMX=512m               # Local: 256m, Dev: 512m, Prod: 2048m

# Non-Heap
JAVA_OPTS_MAX_METASPACE_SIZE=150m      # Local: 128m, Dev: 150m, Prod: 256m
JAVA_OPTS_RESERVED_CODE_CACHE_SIZE=48m # Local: 32m, Dev: 48m, Prod: 128m
JAVA_OPTS_MAX_DIRECT_MEMORY_SIZE=64m   # Local: 32m, Dev: 64m, Prod: 128m
JAVA_OPTS_XSS=512k
```

#### 7. JVM GC Configuration
```bash
JAVA_OPTS_MAX_GC_PAUSE_MILLIS=200          # Prod: 100 (더 낮은 pause time)
JAVA_OPTS_G1_HEAP_REGION_SIZE=2m           # Prod: 4m (더 큰 region)
JAVA_OPTS_INITIATING_HEAP_OCCUPANCY_PERCENT=45  # Prod: 70 (더 늦게 GC 시작)
```

#### 8. JVM Compiler Configuration
```bash
JAVA_OPTS_TIERED_STOP_AT_LEVEL=2    # Prod: 4 (full optimization)
JAVA_OPTS_CI_COMPILER_COUNT=2       # Prod: 4 (more threads)
```

#### 9. Docker Container Resource Limits
```bash
DOCKER_MEMORY_LIMIT=750M             # Local: 500M, Dev: 750M, Prod: 3G
DOCKER_MEMORY_RESERVATION=512M       # Local: 384M, Dev: 512M, Prod: 2G
```

#### 10. Health Check Configuration
```bash
HEALTHCHECK_INTERVAL=30s             # Local: 60s, Dev: 30s, Prod: 20s
HEALTHCHECK_TIMEOUT=15s              # Local: 10s, Dev: 15s, Prod: 10s
HEALTHCHECK_RETRIES=4                # Local: 3, Dev: 4, Prod: 5
HEALTHCHECK_START_PERIOD=120s        # Local: 60s, Dev: 120s, Prod: 180s
```

#### 11. Restart Policy
```bash
RESTART_POLICY_MAX_RETRIES=5         # Local: 3, Dev: 5, Prod: 10
```

#### 12. Autoheal Configuration
```bash
AUTOHEAL_INTERVAL=30                 # Local: 60, Dev: 30, Prod: 20
AUTOHEAL_START_PERIOD=0
AUTOHEAL_DEFAULT_STOP_TIMEOUT=15
AUTOHEAL_MEMORY_LIMIT=50M
AUTOHEAL_MEMORY_RESERVATION=20M
```

#### 13. Logging Configuration
```bash
LOGGING_MAX_SIZE=10m                 # Local: 5m, Dev: 10m, Prod: 50m
LOGGING_MAX_FILE=3                   # Local: 2, Dev: 3, Prod: 10
```

---

## GitHub Secrets 설정 가이드

### 📍 Secrets 위치

GitHub Repository → Settings → Secrets and variables → Actions → Repository secrets

---

### 🔑 필수 Secrets 목록

#### 공통 Secrets (모든 환경)

##### 1. CODECOV_TOKEN
```
용도: 코드 커버리지를 Codecov에 업로드
획득: https://codecov.io/ → Repository 설정 → Upload token
예시: a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

##### 2. DOCKER_USERNAME
```
용도: Docker Hub 로그인
값: Docker Hub 계정 이름
예시: myusername
```

##### 3. DOCKER_PASSWORD
```
용도: Docker Hub 로그인
값: Docker Hub 비밀번호 또는 Access Token (권장)
획득: Docker Hub → Account Settings → Security → New Access Token
예시: dckr_pat_abc123...
```

##### 4. DOCKER_REPO
```
용도: Docker 이미지 저장소 이름
값: 저장소 이름
예시: open-api-batch-server
```

---

#### Dev 환경 Secrets

##### 5. SERVER_HOST
```
용도: 개발 서버 IP 또는 도메인
값: 138.2.126.248
```

##### 6. SERVER_USER
```
용도: 개발 서버 SSH 사용자 이름
값: ubuntu (또는 실제 사용자)
```

##### 7. SERVER_PORT
```
용도: 애플리케이션 포트
값: 8080
```

##### 8. SSH_PRIVATE_KEY
```
용도: 개발 서버 SSH 접속용 비밀키
획득 방법:
  1. 로컬에서 키 생성 (이미 있으면 건너뛰기)
     ssh-keygen -t rsa -b 4096 -C "github-actions"
     → 파일: ~/.ssh/id_rsa

  2. 공개키를 서버에 등록
     ssh-copy-id -i ~/.ssh/id_rsa.pub user@138.2.126.248
     또는 수동으로:
     cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys (서버에서)

  3. 비밀키 복사
     cat ~/.ssh/id_rsa
     -----BEGIN RSA PRIVATE KEY-----
     MIIEpAIBAAKCAQEA...
     ... (전체 내용) ...
     -----END RSA PRIVATE KEY-----

  4. GitHub Secrets에 전체 내용 붙여넣기
     ⚠️ BEGIN/END 포함, 줄바꿈 유지

주의사항:
  - 절대 공개하지 마세요
  - 공개키(.pub)가 아닌 비밀키 사용
  - 권한: chmod 600 ~/.ssh/id_rsa
```

##### 9. ENV_FILE_DEV
```
용도: 개발 서버 환경 변수 (전체 .env.dev 파일 내용)
획득 방법:
  1. .env.dev 파일 열기
  2. 모든 placeholder 값을 실제 값으로 변경
     DB_PASSWORD=your_dev_db_password → DB_PASSWORD=ActualDevPassword123!
     JWT_SECRET_KEY=... → JWT_SECRET_KEY=<실제 32자 이상 키>
     NEXON_OPEN_API_KEY=... → NEXON_OPEN_API_KEY=<실제 API 키>

  3. 전체 파일 내용 복사
     cat .env.dev | pbcopy  (Mac)
     cat .env.dev | clip    (Windows)

  4. GitHub Secrets에 붙여넣기
     ⚠️ 전체 파일 내용, 주석 포함, 줄바꿈 유지

형식 예시:
SERVER_PORT=8080
DB_IP=10.0.1.5
DB_PORT=3306
DB_SCHEMA=devnogi_dev
DB_USER=devnogi_user
DB_PASSWORD=DevPassword123!
JWT_SECRET_KEY=dev-secret-key-min-32-chars-required-12345
JWT_ACCESS_TOKEN_VALIDITY=3600
...
(전체 .env.dev 내용)
```

---

#### Prod 환경 Secrets

##### 10. PROD_SERVER_HOST
```
용도: 운영 서버 IP 또는 도메인
값: 실제 운영 서버 IP (예: 203.0.113.50)
```

##### 11. PROD_SERVER_USER
```
용도: 운영 서버 SSH 사용자 이름
값: ubuntu (또는 실제 사용자)
```

##### 12. PROD_SERVER_PORT
```
용도: 운영 애플리케이션 포트
값: 8080
```

##### 13. PROD_SSH_PRIVATE_KEY
```
용도: 운영 서버 SSH 접속용 비밀키
⚠️ 중요: 개발 서버와 다른 키 사용 권장!

획득 방법: SSH_PRIVATE_KEY와 동일
  1. 새 키 페어 생성
     ssh-keygen -t rsa -b 4096 -C "github-actions-prod" -f ~/.ssh/id_rsa_prod

  2. 공개키를 운영 서버에 등록
     ssh-copy-id -i ~/.ssh/id_rsa_prod.pub user@203.0.113.50

  3. 비밀키 복사
     cat ~/.ssh/id_rsa_prod

  4. GitHub Secrets에 붙여넣기
```

##### 14. ENV_FILE_PROD
```
용도: 운영 서버 환경 변수 (전체 .env.prod 파일 내용)
⚠️ 매우 중요: 운영 환경 값을 신중하게 설정!

획득 방법:
  1. .env.prod 파일 열기
  2. 모든 placeholder 값을 운영 환경 값으로 변경
     - DB_PASSWORD: 강력한 비밀번호 (최소 16자, 특수문자 포함)
     - JWT_SECRET_KEY: 강력한 비밀키 (최소 32자, 랜덤 생성)
       생성: openssl rand -base64 32
     - NEXON_OPEN_API_KEY: 운영용 API 키
     - SPRING_PROFILES_ACTIVE: prod
     - DOCKER_IMAGE_TAG: prod

  3. 메모리 설정 확인
     - JAVA_OPTS_XMS=512m
     - JAVA_OPTS_XMX=2048m
     - DOCKER_MEMORY_LIMIT=3G

  4. 전체 파일 내용 복사 후 GitHub Secrets에 붙여넣기

형식 예시:
SERVER_PORT=8080
DB_IP=10.0.2.10
DB_PORT=3306
DB_SCHEMA=devnogi_prod
DB_USER=devnogi_user
DB_PASSWORD=StrongProdPassword!@#$%123456
JWT_SECRET_KEY=prod-secret-key-must-be-very-strong-and-unique-12345678
JWT_ACCESS_TOKEN_VALIDITY=3600
SPRING_PROFILES_ACTIVE=prod
DOCKER_IMAGE_TAG=prod
JAVA_OPTS_XMS=512m
JAVA_OPTS_XMX=2048m
...
(전체 .env.prod 내용)
```

---

### 📋 Secrets 설정 체크리스트

#### 공통 (4개)
- [ ] CODECOV_TOKEN
- [ ] DOCKER_USERNAME
- [ ] DOCKER_PASSWORD
- [ ] DOCKER_REPO

#### Dev 환경 (5개)
- [ ] SERVER_HOST
- [ ] SERVER_USER
- [ ] SERVER_PORT
- [ ] SSH_PRIVATE_KEY
- [ ] ENV_FILE_DEV

#### Prod 환경 (5개)
- [ ] PROD_SERVER_HOST
- [ ] PROD_SERVER_USER
- [ ] PROD_SERVER_PORT
- [ ] PROD_SSH_PRIVATE_KEY
- [ ] ENV_FILE_PROD

**총 14개 Secrets 필요**

---

### 🔍 Secrets 검증 방법

#### 1. GitHub Actions에서 확인
```yaml
# 워크플로우 파일에 임시 추가 (테스트용)
- name: Verify Secrets
  run: |
    echo "Docker Username: ${{ secrets.DOCKER_USERNAME }}"
    echo "Server Host: ${{ secrets.SERVER_HOST }}"
    # ⚠️ 실제 값은 출력하지 말 것!
    echo "ENV_FILE_DEV exists: ${{ secrets.ENV_FILE_DEV != '' }}"
```

#### 2. SSH 키 테스트
```bash
# 로컬에서 테스트
ssh -i ~/.ssh/id_rsa user@138.2.126.248 "echo 'SSH connection successful'"
```

#### 3. Docker Hub 로그인 테스트
```bash
# 로컬에서 테스트
echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
```

---

### ⚠️ 보안 주의사항

1. **절대 로그에 출력하지 마세요**
   ```yaml
   # ❌ 절대 금지
   - run: echo "${{ secrets.JWT_SECRET_KEY }}"

   # ✅ 존재 여부만 확인
   - run: echo "Secret exists: ${{ secrets.JWT_SECRET_KEY != '' }}"
   ```

2. **ENV_FILE에 실제 값 포함**
   - Placeholder가 아닌 실제 운영 값 사용
   - 모든 필수 변수 포함 확인

3. **운영/개발 분리**
   - PROD와 DEV는 다른 키/비밀번호 사용
   - SSH 키도 분리 권장

4. **정기적인 로테이션**
   - JWT_SECRET_KEY: 6개월마다
   - DB_PASSWORD: 3개월마다
   - SSH 키: 1년마다

---

## CD 파이프라인 실행 흐름

### Dev 환경 배포 전체 시나리오

#### 🚀 Step-by-Step 실행

**1. 개발자가 dev 브랜치에 푸시**
```bash
git checkout dev
git add .
git commit -m "feat: implement new feature"
git push origin dev
```

**2. GitHub Actions 트리거**
```
Event: push to dev branch
Workflow: .github/workflows/push-cd-dev.yml
Runner: ubuntu-latest (GitHub-hosted)
```

**3. CI Stage: 테스트 실행**
```bash
# GitHub Actions Runner에서 실행
./gradlew clean test jacocoTestReport --no-daemon

→ 테스트 통과 ✅
→ 커버리지 리포트 생성
→ Codecov에 업로드
```

**4. Docker 이미지 빌드**
```bash
# Dockerfile 기반 빌드
docker buildx build \
  --cache-from type=gha \
  --cache-to type=gha,mode=max \
  -t username/repo:dev \
  -t username/repo:dev-abc1234 \
  --push .

→ Multi-stage build 실행:
  Stage 1 (builder): JAR 빌드
  Stage 2 (extractor): Layer 추출
  Stage 3 (runtime): 최종 이미지

→ Docker Hub에 푸시 완료
```

**5. SSH 접속 준비**
```bash
# GitHub Actions Runner에서
mkdir -p ~/.ssh
echo "$SSH_PRIVATE_KEY" > ~/.ssh/my-key.pem
chmod 400 ~/.ssh/my-key.pem
ssh-keyscan -H 138.2.126.248 >> ~/.ssh/known_hosts
```

**6. docker-compose.yaml 복사**
```bash
scp -i ~/.ssh/my-key.pem \
  docker-compose.yaml \
  ubuntu@138.2.126.248:/home/ubuntu/app/
```

**7. 서버에서 배포 실행**
```bash
# 서버(138.2.126.248)에서 실행
ssh -i ~/.ssh/my-key.pem ubuntu@138.2.126.248

cd /home/ubuntu/app

# .env 파일 생성
echo "$ENV_FILE_DEV" > .env

# .env 파일 내용:
# SERVER_PORT=8080
# DB_IP=10.0.1.5
# DB_PASSWORD=DevPassword123!
# ...

# 최신 이미지 다운로드
docker pull username/repo:dev

# 기존 컨테이너 중지 및 제거
docker compose down
  → spring-app 컨테이너 중지
  → autoheal 컨테이너 중지
  → 네트워크 삭제

# 새 컨테이너 시작
docker compose up -d
  → app-network 생성
  → spring-app 컨테이너 시작 (백그라운드)
  → autoheal 컨테이너 시작
  → 로그는 ./logs 디렉토리에 저장
```

**8. 컨테이너 시작 과정**
```
docker compose up -d 실행
  ↓
1. 네트워크 생성: app-network
  ↓
2. spring-app 컨테이너 생성
  ↓
3. 환경 변수 주입 (.env 파일 읽기)
  ↓
4. ENTRYPOINT 실행:
   java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher
  ↓
5. Spring Boot 애플리케이션 시작
   - 데이터베이스 연결
   - Flyway 마이그레이션
   - 배치 스케줄러 시작
   - Actuator 엔드포인트 활성화
  ↓
6. Health check 시작 (start_period 120초 후)
   - wget http://localhost:8080/actuator/health
   - 30초마다 체크
   - 4번 연속 실패 시 unhealthy
```

**9. Health Check 검증 (GitHub Actions에서)**
```bash
# 1단계: 컨테이너 실행 확인
CONTAINER_ID=$(docker ps -q --filter "name=spring-app")
if [ -z "$CONTAINER_ID" ]; then
  echo "❌ Container not running"
  exit 1
fi
echo "✅ Container is running (ID: abc123)"

# 2단계: Docker health status 대기 (최대 5분)
for i in {1..30}; do
  HEALTH_STATUS=$(docker inspect --format='{{.State.Health.Status}}' spring-app)

  if [ "$HEALTH_STATUS" == "healthy" ]; then
    echo "✅ Container is healthy"
    break
  fi

  echo "Current health status: $HEALTH_STATUS ($i/30)"
  sleep 10
done

# 3단계: Actuator endpoint 직접 확인 (최대 3분)
for i in {1..20}; do
  HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health)

  if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
    echo "✅ Application health check passed"
    echo "Health response: {\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}"
    break
  fi

  echo "Waiting for application to start... ($i/20)"
  sleep 10
done

echo "=== Health Check Complete ==="
docker ps --filter "name=spring-app"
```

**10. 배포 완료**
```
✅ Deployment successful!
🔗 Dev Server: http://138.2.126.248:8080
🐳 Image: username/repo:dev
📦 Commit: abc1234567890
```

---

### Prod 환경 배포 차이점

**추가 검증 단계**:

```bash
# 4단계: Smoke test (Prod만)
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

if [ "$HTTP_CODE" == "200" ]; then
  echo "✅ Smoke test passed (HTTP 200)"
else
  echo "❌ Smoke test failed (HTTP $HTTP_CODE)"
  exit 1
fi
```

**이미지 태그 차이**:
```
Dev:  username/repo:dev
      username/repo:dev-abc1234

Prod: username/repo:prod
      username/repo:latest
      username/repo:prod-abc1234
```

**환경 변수 차이**:
```
Dev:  ENV_FILE_DEV → .env
Prod: ENV_FILE_PROD → .env

Dev:  DOCKER_IMAGE_TAG=dev
Prod: DOCKER_IMAGE_TAG=prod

Dev:  SPRING_PROFILES_ACTIVE=default
Prod: SPRING_PROFILES_ACTIVE=prod
```

---

## 트러블슈팅

### 문제 1: GitHub Actions에서 "Permission denied (publickey)"

**증상**:
```
Permission denied (publickey).
fatal: Could not read from remote repository.
```

**원인**: SSH 키가 올바르지 않거나 서버에 등록되지 않음

**해결**:
```bash
# 1. 공개키가 서버에 등록되어 있는지 확인
ssh user@server "cat ~/.ssh/authorized_keys"

# 2. 비밀키 포맷 확인
cat ~/.ssh/id_rsa
# -----BEGIN RSA PRIVATE KEY----- 로 시작해야 함

# 3. GitHub Secret 재등록
# Settings → Secrets → SSH_PRIVATE_KEY
# 전체 내용 (BEGIN/END 포함) 복사 붙여넣기
```

---

### 문제 2: Docker 이미지 빌드 실패

**증상**:
```
ERROR: failed to solve: failed to compute cache key
```

**원인**: Gradle 파일이 변경되었거나 캐시 문제

**해결**:
```bash
# 1. GitHub Actions에서 캐시 삭제
# Settings → Actions → Caches → Delete cache

# 2. 워크플로우 재실행
# Actions → 실패한 워크플로우 → Re-run jobs
```

---

### 문제 3: Health Check 실패

**증상**:
```
❌ Application health check failed after 3+ minutes
Last response: {"status":"DOWN"}
```

**원인**: 데이터베이스 연결 실패, 환경 변수 오류 등

**해결**:
```bash
# 1. 서버 로그 확인
ssh user@server
docker logs spring-app --tail 100

# 일반적인 오류:
# - "Access denied for user" → DB_PASSWORD 확인
# - "Unknown database" → DB_SCHEMA 확인
# - "Communications link failure" → DB_IP, DB_PORT 확인

# 2. .env 파일 확인
cat /home/ubuntu/app/.env
# 모든 변수가 올바르게 설정되었는지 확인

# 3. 수동으로 Health check
curl http://localhost:8080/actuator/health

# 4. 컨테이너 재시작
docker compose down
docker compose up -d
```

---

### 문제 4: 컨테이너가 계속 재시작됨

**증상**:
```
docker ps
CONTAINER ID   STATUS
abc123         Restarting (1) 5 seconds ago
```

**원인**: 애플리케이션 시작 실패, OOM, 설정 오류

**해결**:
```bash
# 1. 로그 확인 (재시작 전 로그도 확인)
docker logs spring-app --tail 200

# 2. 리소스 확인
docker stats spring-app
# MEM USAGE가 LIMIT에 근접하면 OOM 가능성

# 3. 메모리 증가
# .env 파일 수정
DOCKER_MEMORY_LIMIT=1G  # 750M → 1G로 증가

# 4. 재배포
docker compose down
docker compose up -d

# 5. Health check 시간 증가
HEALTHCHECK_START_PERIOD=180s  # 120s → 180s
```

---

### 문제 5: ENV_FILE_DEV/PROD 값이 적용되지 않음

**증상**:
```
서버에서 .env 파일을 확인하면 내용이 이상함
또는 환경 변수가 기본값으로 설정됨
```

**원인**: GitHub Secret에 잘못된 형식으로 저장됨

**해결**:
```bash
# 1. GitHub Secret 값 확인
# Settings → Secrets → ENV_FILE_DEV → Update

# 2. 올바른 형식 확인
# - 주석 포함 OK
# - 빈 줄 포함 OK
# - 줄바꿈 유지 필수
# - 특수문자는 따옴표로 감싸기

# 잘못된 예:
AUCTION_HISTORY_CRON=0 0 * * * *  # ❌ 쉘이 * 를 파일 glob으로 해석

# 올바른 예:
AUCTION_HISTORY_CRON="0 0 * * * *"  # ✅ 따옴표로 감싸기

# 3. .env.dev 파일 재확인
cat .env.dev

# 4. GitHub Secret 재등록
# 전체 파일 내용 복사
cat .env.dev | pbcopy  # Mac
cat .env.dev | clip    # Windows

# GitHub에 붙여넣기
```

---

### 문제 6: autoheal이 작동하지 않음

**증상**:
```
컨테이너가 unhealthy 상태인데 재시작되지 않음
```

**원인**: autoheal 컨테이너 문제, label 누락

**해결**:
```bash
# 1. autoheal 컨테이너 상태 확인
docker ps -a | grep autoheal

# 2. autoheal 로그 확인
docker logs autoheal

# 3. spring-app에 label이 있는지 확인
docker inspect spring-app | grep -A 5 Labels
# "autoheal": "true" 있어야 함

# 4. docker-compose.yaml 확인
# spring-app에 다음이 있어야 함:
labels:
  autoheal: "true"

# 5. autoheal 재시작
docker compose restart autoheal
```

---

### 유용한 명령어 모음

```bash
# 컨테이너 상태 확인
docker ps -a

# 실시간 로그 확인
docker logs -f spring-app

# 컨테이너 리소스 사용량
docker stats

# 컨테이너 내부 접속
docker exec -it spring-app sh

# Health check 수동 실행
docker exec spring-app wget --spider http://localhost:8080/actuator/health

# 환경 변수 확인
docker exec spring-app printenv

# 네트워크 확인
docker network ls
docker network inspect app-network

# 이미지 확인
docker images | grep open-api-batch-server

# 특정 태그 이미지 삭제
docker rmi username/repo:dev

# 사용하지 않는 리소스 정리
docker system prune -a
```

---

## 📚 참고 자료

- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Docker Compose 공식 문서](https://docs.docker.com/compose/)
- [Spring Boot Docker 가이드](https://spring.io/guides/gs/spring-boot-docker/)
- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)
- [GitHub Encrypted Secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

---

**작성자**: Claude Code
**마지막 업데이트**: 2025-10-31
**버전**: 1.0
