#!/usr/bin/env bash
set -Eeuo pipefail

subject=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/rebind-prod-elasticsearch.sh
image_id=sha256:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

new_fixture() {
  fixture=$(mktemp -d /tmp/esrollouttestXXXXXXXX)
  remote="$fixture/remote"
  stage="$remote/.elasticsearch-rollout-1-1"
  fake_bin="$fixture/bin"
  fake_state="$fixture/state"
  mkdir -p "$stage" "$fake_bin" "$fake_state"
  cp "$subject" "$stage/remote.sh"
  cat >"$remote/compose.yml" <<'YAML'
services:
  devnogi-elastic-search:
    image: elasticsearch:8.11.0
    ports:
      - "127.0.0.1:${ELASTICSEARCH_PORT:-9200}:9200"
      - "127.0.0.1:${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - app-network
volumes:
  elasticsearch_data:
    driver: local
networks:
  app-network:
    driver: bridge
YAML
  : >"$remote/.env.prod"
  printf 'public\n' >"$fake_state/mode"
  printf 'healthy\n' >"$fake_state/health"
  printf 'batch_elasticsearch_data\n' >"$fake_state/volume"
  printf 'batch_app-network\n' >"$fake_state/network"
  printf '0\n' >"$fake_state/up-count"

  cat >"$fake_bin/docker" <<'BASH'
#!/usr/bin/env bash
set -Eeuo pipefail
state=$FAKE_STATE
if [[ $1 == inspect ]]; then
  format=$3
  case "$format" in
    *com.docker.compose.project.config_files*) printf '%s/compose.yml\n' "$REMOTE_DIR" ;;
    *com.docker.compose.project.working_dir*) printf '%s\n' "$REMOTE_DIR" ;;
    *com.docker.compose.project*) printf 'batch\n' ;;
    *com.docker.compose.service*) printf 'devnogi-elastic-search\n' ;;
    *'.Image'*) printf '%s\n' "$IMAGE_ID" ;;
    *'.Mounts'*) printf 'volume|%s|/usr/share/elasticsearch/data\n' "$(<"$state/volume")" ;;
    *'.NetworkSettings.Networks'*) printf '%s\n' "$(<"$state/network")" ;;
    *'9200/tcp'*)
      case $(<"$state/mode") in
        public) printf '0.0.0.0|9200\n::|9200\n' ;;
        loopback) printf '127.0.0.1|9200\n' ;;
        mixed) printf '0.0.0.0|9200\n127.0.0.1|9200\n' ;;
      esac ;;
    *'9300/tcp'*)
      case $(<"$state/mode") in
        public) printf '0.0.0.0|9300\n::|9300\n' ;;
        loopback) printf '127.0.0.1|9300\n' ;;
        mixed) printf '0.0.0.0|9300\n127.0.0.1|9300\n' ;;
      esac ;;
    *'.State.Status'*) printf 'running|%s\n' "$(<"$state/health")" ;;
    *) exit 90 ;;
  esac
  exit 0
fi
if [[ $1 == port ]]; then
  mode=$(<"$state/mode")
  port=${3%/tcp}
  [[ $mode == loopback ]] && printf '127.0.0.1:%s\n' "$port" || printf '0.0.0.0:%s\n[::]:%s\n' "$port" "$port"
  exit 0
fi
[[ $1 == compose ]] || exit 91
shift
base=
operation=
format_json=0
while (($#)); do
  case $1 in
    -f) [[ -z $base ]] && base=$2; shift 2 ;;
    config) operation=config; shift ;;
    up) operation=up; shift ;;
    --format) [[ $2 == json ]] && format_json=1; shift 2 ;;
    *) shift ;;
  esac
done
if [[ $operation == config ]]; then
  if ((format_json)); then
    printf '{"services":{"devnogi-elastic-search":{"volumes":[{"type":"volume","source":"elasticsearch_data","target":"/usr/share/elasticsearch/data"}],"networks":{"app-network":{}}}},"volumes":{"elasticsearch_data":{"name":"batch_elasticsearch_data"}},"networks":{"app-network":{"name":"batch_app-network"}}}\n'
  fi
  exit 0
fi
count=$(<"$state/up-count"); printf '%s\n' "$((count + 1))" >"$state/up-count"
if [[ $base == *elasticsearch-rollback-* ]]; then
  [[ ${FAIL_ROLLBACK:-0} == 1 ]] && exit 42
  printf 'public\n' >"$state/mode"
  printf 'healthy\n' >"$state/health"
  printf 'batch_elasticsearch_data\n' >"$state/volume"
  printf 'batch_app-network\n' >"$state/network"
  exit 0
fi
[[ ${FAIL_RECREATE:-0} == 1 ]] && exit 41
printf 'loopback\n' >"$state/mode"
printf '%s\n' "${ACTIVATED_HEALTH:-healthy}" >"$state/health"
[[ ${CHANGE_RESOURCE:-0} == 1 ]] && printf 'wrong-network\n' >"$state/network"
touch "$state/activation-started"
[[ ${INTERRUPT_ACTIVATION:-0} == 1 ]] && sleep 3
exit 0
BASH
  cat >"$fake_bin/curl" <<'BASH'
#!/usr/bin/env bash
[[ ${FAIL_CURL:-0} == 1 ]] && exit 22
printf '{"status":"yellow"}\n'
BASH
  cat >"$fake_bin/flock" <<'BASH'
#!/usr/bin/env bash
[[ ${3:-} == 8 ]] && sleep 4
exit 0
BASH
  cat >"$fake_bin/python3" <<'BASH'
#!/usr/bin/env bash
cat >/dev/null
printf 'batch_elasticsearch_data\nbatch_app-network\n'
BASH
  chmod +x "$fake_bin/docker" "$fake_bin/curl" "$fake_bin/flock" "$fake_bin/python3" "$stage/remote.sh"
  compose_sha=$(sha256sum "$remote/compose.yml" | awk '{print $1}')
}

run_rollout() {
  set +e
  PATH="$fake_bin:$PATH" FAKE_STATE="$fake_state" REMOTE_DIR="$remote" IMAGE_ID="$image_id" \
    ELASTICSEARCH_HEALTH_ATTEMPTS=1 ELASTICSEARCH_HEALTH_INTERVAL_SECONDS=0 ELASTICSEARCH_WATCHDOG_SECONDS=10 \
    "$stage/remote.sh" rollout "$remote" "$compose_sha" "$stage"
  status=$?
  set -e
}

assert_result() {
  [[ $(<"$stage/result") == "ROLLOUT_RESULT=$1" ]]
}

new_fixture
run_rollout
[[ $status -eq 0 ]]; assert_result SUCCESS
rm -rf "$fixture"

new_fixture
FAIL_RECREATE=1 run_rollout
[[ $status -eq 1 ]]; assert_result ROLLED_BACK
[[ $(<"$fake_state/mode") == public ]]
rm -rf "$fixture"

new_fixture
ACTIVATED_HEALTH=unhealthy run_rollout
[[ $status -eq 1 ]]; assert_result ROLLED_BACK
rm -rf "$fixture"

new_fixture
printf 'mixed\n' >"$fake_state/mode"
run_rollout
[[ $status -ne 0 && ! -e $stage/result && $(<"$fake_state/up-count") == 0 ]]
rm -rf "$fixture"

new_fixture
printf 'wrong-volume\n' >"$fake_state/volume"
run_rollout
[[ $status -ne 0 && ! -e $stage/result && $(<"$fake_state/up-count") == 0 ]]
rm -rf "$fixture"

new_fixture
FAIL_RECREATE=1 FAIL_ROLLBACK=1 run_rollout
[[ $status -eq 2 ]]; assert_result ROLLBACK_FAILED
rm -rf "$fixture"

new_fixture
PATH="$fake_bin:$PATH" FAKE_STATE="$fake_state" REMOTE_DIR="$remote" IMAGE_ID="$image_id" \
  ELASTICSEARCH_HEALTH_ATTEMPTS=1 ELASTICSEARCH_HEALTH_INTERVAL_SECONDS=0 ELASTICSEARCH_WATCHDOG_SECONDS=10 \
  INTERRUPT_ACTIVATION=1 "$stage/remote.sh" rollout "$remote" "$compose_sha" "$stage" &
rollout_pid=$!
for _ in $(seq 1 30); do [[ -f $fake_state/activation-started ]] && break; sleep 0.1; done
[[ -f $fake_state/activation-started ]]
kill -KILL "$rollout_pid"
wait "$rollout_pid" 2>/dev/null || true
for _ in $(seq 1 100); do [[ -f $stage/result ]] && break; sleep 0.1; done
assert_result ROLLED_BACK
[[ $(<"$fake_state/mode") == public ]]
rm -rf "$fixture"

echo 'Elasticsearch rollout execution tests passed'
