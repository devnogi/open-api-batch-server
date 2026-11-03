#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

container=devnogi-elastic-search
service=devnogi-elastic-search

valid_path() { [[ ${1:-} =~ ^/([a-zA-Z0-9_.-]+/)*[a-zA-Z0-9_.-]+$ && ${1:-} != *..* ]]; }
valid_project() { [[ ${1:-} =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ ]]; }
valid_resource() { [[ ${1:-} =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ ]]; }

inspect_networks() {
  docker inspect --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}}{{"\n"}}{{end}}' "$container" | sed '/^$/d' | sort
}

inspect_bindings() {
  local port=$1
  docker inspect --format "{{with index .HostConfig.PortBindings \"$port/tcp\"}}{{range .}}{{.HostIp}}|{{.HostPort}}{{\"\\n\"}}{{end}}{{end}}" "$container" | sed '/^$/d' | sort
}

valid_bindings() {
  local bindings=$1 binding count=0
  while IFS= read -r binding; do
    [[ $binding =~ ^(0\.0\.0\.0|127\.0\.0\.1|::)\|[1-9][0-9]{0,4}$ ]] || return 1
    ((count += 1))
  done <<<"$bindings"
  ((count > 0))
}

binding_mode() {
  local bindings=$1 line ipv4=0 ipv6=0 loopback=0 published= value
  while IFS= read -r line; do
    value=${line#*|}
    [[ -z $published || $published == "$value" ]] || return 1
    published=$value
    case ${line%%|*} in
      0.0.0.0) ((ipv4 += 1)) ;;
      ::) ((ipv6 += 1)) ;;
      127.0.0.1) ((loopback += 1)) ;;
      *) return 1 ;;
    esac
  done <<<"$bindings"
  if ((loopback == 1 && ipv4 == 0 && ipv6 == 0)); then
    printf 'loopback'
  elif ((loopback == 0 && ipv4 == 1 && ipv6 == 1)); then
    printf 'public'
  else
    return 1
  fi
}

healthy() {
  [[ $(docker inspect --format '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{end}}' "$container") == 'running|healthy' ]]
}

wait_for_health() {
  local attempts=${ELASTICSEARCH_HEALTH_ATTEMPTS:-60}
  local interval=${ELASTICSEARCH_HEALTH_INTERVAL_SECONDS:-3}
  local count
  [[ $attempts =~ ^[1-9][0-9]?$ && $attempts -le 60 ]]
  [[ $interval =~ ^[0-9]$ ]]
  for count in $(seq 1 "$attempts"); do
    healthy && return 0
    sleep "$interval"
  done
  return 1
}

load_state() {
  project=$(<"$state_dir/project")
  previous_image_id=$(<"$state_dir/image")
  previous_mount=$(<"$state_dir/mount")
  previous_networks=$(<"$state_dir/networks")
  previous_http_bindings=$(<"$state_dir/http-bindings")
  previous_transport_bindings=$(<"$state_dir/transport-bindings")
  rollback_compose=$(<"$state_dir/rollback-compose")
  image_override=$(<"$state_dir/image-override")
  valid_project "$project"
  [[ $previous_image_id =~ ^sha256:[a-f0-9]{64}$ ]]
  [[ $previous_mount =~ ^volume\|[a-zA-Z0-9_.-]+\|/usr/share/elasticsearch/data$ ]]
  valid_bindings "$previous_http_bindings"
  valid_bindings "$previous_transport_bindings"
  valid_path "$rollback_compose" && valid_path "$image_override"
}

same_identity() {
  [[ $(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' "$container") == "$project" ]] &&
    [[ $(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}' "$container") == "$remote_dir" ]]
}

same_resources() {
  local image mount networks
  image=$(docker inspect --format '{{.Image}}' "$container") || return 1
  mount=$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/usr/share/elasticsearch/data"}}{{.Type}}|{{.Name}}|{{.Destination}}{{end}}{{end}}' "$container") || return 1
  networks=$(inspect_networks) || return 1
  [[ $image == "$previous_image_id" && $mount == "$previous_mount" && $networks == "$previous_networks" ]]
}

same_bindings() {
  [[ $(inspect_bindings 9200) == "$previous_http_bindings" ]] &&
    [[ $(inspect_bindings 9300) == "$previous_transport_bindings" ]]
}

record_result() {
  local result=$1 temporary="$state_dir/result.tmp.$$"
  printf 'ROLLOUT_RESULT=%s\n' "$result" >"$temporary"
  mv "$temporary" "$state_dir/result"
}

recreate() {
  local base_compose=$1
  docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod \
    -f "$base_compose" -f "$image_override" config --quiet
  docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod \
    -f "$base_compose" -f "$image_override" up -d --pull never --no-deps --force-recreate "$service"
}

rollback() {
  if recreate "$rollback_compose" && wait_for_health && same_identity && same_resources && same_bindings; then
    record_result ROLLED_BACK
    return 0
  fi
  record_result ROLLBACK_FAILED
  return 1
}

watchdog_main() {
  remote_dir=${2:-}
  state_dir=${3:-}
  parent_pid=${4:-}
  local timeout=${ELASTICSEARCH_WATCHDOG_SECONDS:-420} elapsed=0
  valid_path "$remote_dir" && valid_path "$state_dir"
  [[ $parent_pid =~ ^[1-9][0-9]*$ ]]
  [[ $timeout =~ ^[1-9][0-9]{0,2}$ && $timeout -le 600 ]]
  exec 9>&- || true

  while [[ ! -f $state_dir/result ]] && kill -0 "$parent_pid" 2>/dev/null && ((elapsed < timeout)); do
    sleep 1
    ((elapsed += 1))
  done
  [[ -f $state_dir/result ]] && exit 0
  if kill -0 "$parent_pid" 2>/dev/null; then
    kill -TERM "$parent_pid" 2>/dev/null || true
    for _ in $(seq 1 15); do
      [[ -f $state_dir/result ]] && exit 0
      kill -0 "$parent_pid" 2>/dev/null || break
      sleep 1
    done
    kill -KILL "$parent_pid" 2>/dev/null || true
  fi

  exec 8>"$remote_dir/.deploy.lock"
  flock -w 30 8 || { record_result ROLLBACK_FAILED; exit 2; }
  [[ -f $state_dir/result ]] && exit 0
  cd "$remote_dir"
  load_state || { record_result ROLLBACK_FAILED; exit 2; }
  rollback || exit 2
  exit 1
}

rollout_main() {
  remote_dir=${2:-}
  expected_compose_sha256=${3:-}
  state_dir=${4:-}
  valid_path "$remote_dir" && valid_path "$state_dir"
  [[ $state_dir == "$remote_dir"/.elasticsearch-rollout-* ]]
  [[ $expected_compose_sha256 =~ ^[a-f0-9]{64}$ ]]
  cd "$remote_dir"
  [[ $PWD == "$remote_dir" && -f compose.yml && -f .env.prod && -f "$state_dir/remote.sh" ]]

  exec 9>.deploy.lock
  flock -w 30 9
  [[ $(sha256sum compose.yml | awk '{print $1}') == "$expected_compose_sha256" ]]

  project=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' "$container")
  working_dir=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}' "$container")
  config_files=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.config_files"}}' "$container")
  compose_service=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.service"}}' "$container")
  valid_project "$project"
  [[ $working_dir == "$remote_dir" ]]
  if [[ $config_files == "$remote_dir/docker-compose-prod.yml" ]]; then
    [[ ! -e $remote_dir/docker-compose-prod.yml || $remote_dir/docker-compose-prod.yml -ef compose.yml ||
       $(sha256sum "$remote_dir/docker-compose-prod.yml" | awk '{print $1}') == "$expected_compose_sha256" ]]
  else
    [[ $config_files == "$remote_dir/compose.yml" ]]
  fi
  [[ $compose_service == "$service" ]]

  previous_image_id=$(docker inspect --format '{{.Image}}' "$container")
  previous_mount=$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/usr/share/elasticsearch/data"}}{{.Type}}|{{.Name}}|{{.Destination}}{{end}}{{end}}' "$container")
  previous_networks=$(inspect_networks)
  previous_http_bindings=$(inspect_bindings 9200)
  previous_transport_bindings=$(inspect_bindings 9300)
  [[ $previous_image_id =~ ^sha256:[a-f0-9]{64}$ ]]
  [[ $previous_mount =~ ^volume\|([a-zA-Z0-9_.-]+)\|/usr/share/elasticsearch/data$ ]]
  previous_volume=${BASH_REMATCH[1]}
  valid_bindings "$previous_http_bindings" && valid_bindings "$previous_transport_bindings"
  http_mode=$(binding_mode "$previous_http_bindings")
  transport_mode=$(binding_mode "$previous_transport_bindings")
  [[ $http_mode == "$transport_mode" ]]

  rendered=$(docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod -f compose.yml config --format json)
  mapfile -t candidates < <(python3 -c '
import json, sys
c=json.load(sys.stdin); s=c["services"]["devnogi-elastic-search"]
v=[x for x in s.get("volumes",[]) if x.get("target")=="/usr/share/elasticsearch/data" and x.get("type")=="volume"]
n=sorted(s.get("networks",{}))
assert len(v)==1 and len(n)==1
print(c["volumes"][v[0]["source"]]["name"])
print(c["networks"][n[0]]["name"])
' <<<"$rendered")
  [[ ${#candidates[@]} -eq 2 ]]
  candidate_volume=${candidates[0]}
  candidate_network=${candidates[1]}
  valid_resource "$candidate_volume" && valid_resource "$candidate_network"
  [[ $previous_volume == "$candidate_volume" && $previous_networks == "$candidate_network" ]]

  timestamp=$(date -u +%Y%m%dT%H%M%SZ)
  rollback_compose="$remote_dir/elasticsearch-rollback-$timestamp.yml"
  image_override="$state_dir/image-override.yml"
  [[ ! -e $rollback_compose && ! -e $image_override && ! -e $state_dir/result ]]
  cp compose.yml "$rollback_compose"
  printf 'services:\n  %s:\n    image: %s\n' "$service" "$previous_image_id" >"$image_override"
  if [[ $http_mode == public ]]; then
    grep -Fqx '      - "127.0.0.1:${ELASTICSEARCH_PORT:-9200}:9200"' "$rollback_compose"
    grep -Fqx '      - "127.0.0.1:${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"' "$rollback_compose"
    sed -i \
      -e 's|"127.0.0.1:${ELASTICSEARCH_PORT:-9200}:9200"|"${ELASTICSEARCH_PORT:-9200}:9200"|' \
      -e 's|"127.0.0.1:${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"|"${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"|' \
      "$rollback_compose"
  fi
  sed -i "/^  elasticsearch_data:$/a\\    name: $previous_volume" "$rollback_compose"
  sed -i "/^  app-network:$/a\\    name: $candidate_network" "$rollback_compose"
  chmod 600 "$rollback_compose" "$image_override"

  docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod \
    -f compose.yml -f "$image_override" config --quiet
  rollback_rendered=$(docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod \
    -f "$rollback_compose" -f "$image_override" config --format json)
  mapfile -t rollback_candidates < <(python3 -c '
import json, sys
c=json.load(sys.stdin); s=c["services"]["devnogi-elastic-search"]
v=[x for x in s.get("volumes",[]) if x.get("target")=="/usr/share/elasticsearch/data" and x.get("type")=="volume"]
n=sorted(s.get("networks",{}))
assert len(v)==1 and len(n)==1
print(c["volumes"][v[0]["source"]]["name"])
print(c["networks"][n[0]]["name"])
' <<<"$rollback_rendered")
  [[ ${rollback_candidates[0]:-} == "$previous_volume" && ${rollback_candidates[1]:-} == "$candidate_network" ]]

  printf '%s\n' "$project" >"$state_dir/project"
  printf '%s\n' "$previous_image_id" >"$state_dir/image"
  printf '%s\n' "$previous_mount" >"$state_dir/mount"
  printf '%s\n' "$previous_networks" >"$state_dir/networks"
  printf '%s\n' "$previous_http_bindings" >"$state_dir/http-bindings"
  printf '%s\n' "$previous_transport_bindings" >"$state_dir/transport-bindings"
  printf '%s\n' "$rollback_compose" >"$state_dir/rollback-compose"
  printf '%s\n' "$image_override" >"$state_dir/image-override"
  chmod 600 "$state_dir"/*

  nohup env ELASTICSEARCH_HEALTH_ATTEMPTS="${ELASTICSEARCH_HEALTH_ATTEMPTS:-60}" \
    ELASTICSEARCH_HEALTH_INTERVAL_SECONDS="${ELASTICSEARCH_HEALTH_INTERVAL_SECONDS:-3}" \
    ELASTICSEARCH_WATCHDOG_SECONDS="${ELASTICSEARCH_WATCHDOG_SECONDS:-420}" \
    bash "$state_dir/remote.sh" watchdog "$remote_dir" "$state_dir" "$$" </dev/null >/dev/null 2>&1 &
  watchdog_pid=$!

  recover() {
    trap - ERR HUP INT TERM
    set +e
    load_state
    rollback
    local status=$?
    wait "$watchdog_pid" 2>/dev/null || true
    ((status == 0)) && exit 1
    exit 2
  }
  trap 'recover' ERR HUP INT TERM

  recreate compose.yml
  wait_for_health
  same_identity
  same_resources
  loopback_http=$(docker port "$container" 9200/tcp)
  loopback_transport=$(docker port "$container" 9300/tcp)
  [[ $loopback_http =~ ^127\.0\.0\.1:([1-9][0-9]{0,4})$ ]]
  http_port=${BASH_REMATCH[1]}
  [[ $loopback_transport =~ ^127\.0\.0\.1:[1-9][0-9]{0,4}$ ]]
  response=$(curl --fail --silent --show-error --max-time 10 "http://127.0.0.1:$http_port/_cluster/health")
  grep -Eq '"status"[[:space:]]*:[[:space:]]*"(green|yellow)"' <<<"$response"

  trap - ERR HUP INT TERM
  record_result SUCCESS
  wait "$watchdog_pid" 2>/dev/null || true
  rm -f "$image_override"
}

case ${1:-} in
  rollout) rollout_main "$@" ;;
  watchdog) watchdog_main "$@" ;;
  *) exit 1 ;;
esac
