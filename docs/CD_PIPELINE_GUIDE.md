# CI/CD 파이프라인

PR 검증과 운영 배포는 고정된 revision의 `devnogi/ci-workflows` Spring 공통 workflow를 호출한다. `pr-ci.yml`은 dev/main 대상 PR과 수동 진단에서 Batch verify, secret scan, Docker 빌드 및 합성 값으로 운영 Compose 검증을 실행한다. 생성된 JaCoCo 보고서는 artifact로 보존하며 Codecov는 필수 검사가 아니다.

`push-cd-prod.yml`은 main push와 main 수동 실행만 허용한다. 공통 workflow가 검증 후 Gradle 프로젝트 버전으로 이미지 태그를 정하고 동일 실행의 digest를 `IMAGE_REF`로 전달해 `spring-app`만 교체한다. 기존 Docker 및 Actuator health 검사와 제한 시간 300초를 사용하며 실패 시 같은 실행이 보관한 이전 상태를 복구하고 health를 확인한다. main 병합은 자동 운영 배포를 수반하므로 별도 사람 승인이 필요하다.

운영 Environment와 Docker Hub의 설정, 불변 태그 정책 및 공통 workflow 접근 권한이 사전에 필요하다. 공통 workflow는 `PROD2_HOST_FINGERPRINT`를 신뢰 기준으로 사용하며 스캔한 Ed25519 host key의 SHA-256 fingerprint가 일치할 때만 인증을 진행한다. 실제 fingerprint, SSH key, 운영 환경 파일은 저장소에 기록하지 않는다. 운영 Compose의 `IMAGE_REF`는 필수이고 `PROD2_ENV_FILE`에 별도로 정의하지 않는다.

기존 개발 배포는 `push-cd-dev.yml`을 사용한다. 개발 workflow의 설정과 실행 범위는 해당 파일을 기준으로 확인한다. 운영 배포 실패 시 workflow 결과에서 복구 성공 여부를 확인하고 복구 실패 또는 결과 미확인 상태는 운영자가 조사한다.
