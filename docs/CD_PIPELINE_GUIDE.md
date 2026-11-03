# CI/CD 파이프라인

PR 검증과 운영 배포는 고정된 revision의 `devnogi/ci-workflows` Spring 공통 workflow를 호출한다. `pr-ci.yml`은 dev/main 대상 PR과 수동 진단에서 Batch verify, secret scan, Docker 빌드 및 합성 값으로 운영 Compose 검증을 실행한다. 생성된 JaCoCo 보고서는 artifact로 보존하며 Codecov는 필수 검사가 아니다.

`push-cd-prod.yml`은 main push와 main 수동 실행만 허용한다. 공통 workflow가 검증 후 Gradle 프로젝트 버전으로 이미지 태그를 정하고 동일 실행의 digest를 `IMAGE_REF`로 전달해 `spring-app`만 교체한다. 기존 Docker 및 Actuator health 검사와 제한 시간 300초를 사용하며 실패 시 같은 실행이 보관한 이전 상태를 복구하고 health를 확인한다. main 병합은 자동 운영 배포를 수반하므로 별도 사람 승인이 필요하다.

운영 Environment와 Docker Hub의 설정, 불변 태그 정책 및 공통 workflow 접근 권한이 사전에 필요하다. 공통 workflow는 `PROD2_HOST_FINGERPRINT`를 신뢰 기준으로 사용하며 스캔한 Ed25519 host key의 SHA-256 fingerprint가 일치할 때만 인증을 진행한다. 실제 fingerprint, SSH key, 운영 환경 파일은 저장소에 기록하지 않는다. 운영 Compose의 `IMAGE_REF`는 필수이고 `PROD2_ENV_FILE`에 별도로 정의하지 않는다.

기존 개발 배포는 `push-cd-dev.yml`을 사용한다. 개발 workflow의 설정과 실행 범위는 해당 파일을 기준으로 확인한다. 운영 배포 실패 시 workflow 결과에서 복구 성공 여부를 확인하고 복구 실패 또는 결과 미확인 상태는 운영자가 조사한다.

## Elasticsearch loopback 바인딩 1회 롤아웃

공통 운영 배포는 `docker compose ... up -d --no-deps spring-app`만 실행한다. 따라서 `docker-compose-prod.yml`의 Elasticsearch 9200/9300 loopback 변경은 일반 Batch 배포만으로 적용되지 않으며, Compose 파일이 운영 서버에 반영된 뒤 `devnogi-elastic-search`를 한 번 별도로 재생성해야 한다.

병합 전에 운영 원격 디렉터리에서 현재 `compose.yml`을 root 전용 파일로 보관하고 정확한 절대 경로를 운영 기록에 남긴다.

```sh
umask 077
rollback_compose="$PWD/origin-port-compose.$(date -u +%Y%m%dT%H%M%SZ).yml"
cp compose.yml "$rollback_compose"
test -s "$rollback_compose"
```

일반 Batch 배포가 성공한 뒤 동일한 원격 디렉터리와 Compose project에서 Elasticsearch image ID, volume mount, network와 health를 기록하고 다음 순서로 진행한다. `rollback_compose`에는 병합 전에 보관한 파일의 정확한 절대 경로를 넣는다. 실제 환경값이나 inspect 결과를 저장소·로그·채팅에 복사하지 않는다.

```sh
umask 077
exec 9>.deploy.lock
flock -w 30 9

test -f compose.yml
test -f .env.prod
rollback_compose=/absolute/path/recorded-before-merge.yml
test -s "$rollback_compose"
previous_image_id=$(docker inspect --format '{{.Image}}' devnogi-elastic-search)
previous_mounts=$(docker inspect --format '{{range .Mounts}}{{.Name}}:{{.Destination}} {{end}}' devnogi-elastic-search)
previous_networks=$(docker inspect --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}} {{end}}' devnogi-elastic-search)
test -n "$previous_image_id"
test -n "$previous_mounts"
test -n "$previous_networks"

override=$(mktemp ./origin-port-es.XXXXXX.yml)
trap 'rm -f "$override"' EXIT
printf 'services:\n  devnogi-elastic-search:\n    image: %s\n' "$previous_image_id" >"$override"

IMAGE_REF=$(sed -n 's/^IMAGE_REF=//p' .env.prod)
export IMAGE_REF
test -n "$IMAGE_REF"
docker compose --project-directory "$PWD" --env-file .env.prod -f compose.yml -f "$override" config --quiet
docker compose --project-directory "$PWD" --env-file .env.prod -f compose.yml -f "$override" up -d --pull never --no-deps --force-recreate devnogi-elastic-search
```

재생성 후 image ID가 `previous_image_id`와 같고, mount와 network가 기록값과 같으며, container health가 `healthy`인지 확인한다. `docker port devnogi-elastic-search`가 9200과 9300을 모두 `127.0.0.1`에만 표시해야 한다. Batch actuator health, 뿔피리 검색과 인덱스 상태도 확인한다. 외부 호스트에서 두 포트가 닫히고 운영 호스트의 `127.0.0.1:9200`은 응답하는지 별도로 검사한다.

검증 실패 시 보관한 이전 Compose를 첫 번째 `-f`로 사용하고 같은 image override, project directory, `.env.prod`로 `devnogi-elastic-search`만 다시 재생성한다.

```sh
docker compose --project-directory "$PWD" --env-file .env.prod -f "$rollback_compose" -f "$override" up -d --pull never --no-deps --force-recreate devnogi-elastic-search
```

volume을 삭제하거나 `docker compose down -v`를 실행하지 않는다. 복구 후 image, mount, network, health와 Batch 기능을 다시 확인한다.
