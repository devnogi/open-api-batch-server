# Multi-Stage Dockerfile for Spring Boot Application
# Stage 1: Build Stage - Gradle을 사용하여 애플리케이션 빌드
# Stage 2: Extract Stage - Spring Boot Layered JAR 추출
# Stage 3: Runtime Stage - 최종 런타임 이미지

# Stage 1: Build Stage
# alpine 제거하여 ARM64(Apple Silicon)와 AMD64(Intel/AMD) 모두 지원
FROM gradle:8.5-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 의존성 다운로드를 위한 파일만 먼저 복사 (레이어 캐싱 최적화)
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle clean bootJar -x test --no-daemon

# JAR 파일 위치 확인 및 이름 변경
RUN mkdir -p /app/build/extracted && \
    cp /app/build/libs/*.jar /app/build/app.jar

# Stage 2: Extract Layers
FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/app.jar app.jar

# Spring Boot Layered JAR 추출 (레이어 최적화)
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Final Runtime Stage
FROM eclipse-temurin:21-jre-alpine

# 메타데이터 추가
LABEL maintainer="DevNogi Team"
LABEL description="Open API Batch Server - Mabinogi Auction Data Collector"
LABEL version="0.0.1"

# 보안: non-root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring

# 작업 디렉토리 설정
WORKDIR /app

# 레이어별로 복사 (의존성 변경 시 캐시 활용)
COPY --from=extractor --chown=spring:spring /app/dependencies/ ./
COPY --from=extractor --chown=spring:spring /app/spring-boot-loader/ ./
COPY --from=extractor --chown=spring:spring /app/snapshot-dependencies/ ./
COPY --from=extractor --chown=spring:spring /app/application/ ./

# 로그 디렉토리 생성 및 권한 설정 (Named volume 마운트 전에 실행됨)
RUN mkdir -p /app/logs /app/logs/archive && \
    chown -R spring:spring /app/logs

# 사용자 전환
USER spring:spring

# JVM 메모리 설정 환경변수 (기본값, docker-compose에서 오버라이드 중)
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행 (환경변수 JAVA_OPTS 사용)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
