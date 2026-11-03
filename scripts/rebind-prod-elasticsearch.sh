#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

remote_dir=${1:-}
expected_compose_sha256=${2:-}
container=devnogi-elastic-search
service=devnogi-elastic-search

[[ $remote_dir =~ ^/([a-zA-Z0-9_-]+/)*[a-zA-Z0-9_-]+$ ]] || exit 1
[[ $expected_compose_sha256 =~ ^[a-f0-9]{64}$ ]] || exit 1
cd "$remote_dir"
[[ $PWD == "$remote_dir" && -f compose.yml && -f .env.prod ]]
[[ $(sha256sum compose.yml | awk '{print $1}') == "$expected_compose_sha256" ]] || exit 1

exec 9>.deploy.lock
flock -w 30 9

project=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' "$container")
working_dir=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}' "$container")
config_files=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.config_files"}}' "$container")
compose_service=$(docker inspect --format '{{index .Config.Labels "com.docker.compose.service"}}' "$container")
[[ $project =~ ^[a-zA-Z0-9][a-zA-Z0-9_.-]*$ ]]
[[ $working_dir == "$remote_dir" ]]
[[ $config_files == "$remote_dir/compose.yml" ]]
[[ $compose_service == "$service" ]]

previous_image_id=$(docker inspect --format '{{.Image}}' "$container")
previous_mount=$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/usr/share/elasticsearch/data"}}{{.Type}}|{{.Name}}|{{.Destination}}{{end}}{{end}}' "$container")
previous_networks=$(docker inspect --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}}{{"\n"}}{{end}}' "$container" | sed '/^$/d' | sort)
previous_http_bindings=$(docker inspect --format '{{with index .HostConfig.PortBindings "9200/tcp"}}{{range .}}{{.HostIp}}|{{.HostPort}}{{"\n"}}{{end}}{{end}}' "$container" | sed '/^$/d' | sort)
previous_transport_bindings=$(docker inspect --format '{{with index .HostConfig.PortBindings "9300/tcp"}}{{range .}}{{.HostIp}}|{{.HostPort}}{{"\n"}}{{end}}{{end}}' "$container" | sed '/^$/d' | sort)
[[ $previous_image_id =~ ^sha256:[a-f0-9]{64}$ ]]
[[ $previous_mount =~ ^volume\|[a-zA-Z0-9_.-]+\|/usr/share/elasticsearch/data$ ]]
[[ -n $previous_networks ]]

valid_bindings() {
  local bindings=$1 binding count=0
  while IFS= read -r binding; do
    [[ $binding =~ ^(0\.0\.0\.0|127\.0\.0\.1|::)\|[1-9][0-9]{0,4}$ ]] || return 1
    ((count += 1))
  done <<<"$bindings"
  ((count > 0))
}
valid_bindings "$previous_http_bindings"
valid_bindings "$previous_transport_bindings"

timestamp=$(date -u +%Y%m%dT%H%M%SZ)
rollback_compose="$remote_dir/elasticsearch-rollback-$timestamp.yml"
image_override="$remote_dir/elasticsearch-image-$timestamp.yml"
[[ ! -e $rollback_compose && ! -e $image_override ]]
cp compose.yml "$rollback_compose"
printf 'services:\n  %s:\n    image: %s\n' "$service" "$previous_image_id" >"$image_override"
if [[ $previous_http_bindings != 127.0.0.1\|* || $previous_http_bindings == *$'\n'* ||
      $previous_transport_bindings != 127.0.0.1\|* || $previous_transport_bindings == *$'\n'* ]]; then
  grep -Fqx '      - "127.0.0.1:${ELASTICSEARCH_PORT:-9200}:9200"' "$rollback_compose"
  grep -Fqx '      - "127.0.0.1:${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"' "$rollback_compose"
  sed -i \
    -e 's|"127.0.0.1:${ELASTICSEARCH_PORT:-9200}:9200"|"${ELASTICSEARCH_PORT:-9200}:9200"|' \
    -e 's|"127.0.0.1:${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"|"${ELASTICSEARCH_TRANSPORT_PORT:-9300}:9300"|' \
    "$rollback_compose"
fi
chmod 600 "$rollback_compose" "$image_override"

compose=(docker compose --project-name "$project" --project-directory "$remote_dir" --env-file .env.prod)
"${compose[@]}" -f compose.yml -f "$image_override" config --quiet
"${compose[@]}" -f "$rollback_compose" -f "$image_override" config --quiet

same_identity() {
  [[ $(docker inspect --format '{{index .Config.Labels "com.docker.compose.project"}}' "$container") == "$project" ]] &&
    [[ $(docker inspect --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}' "$container") == "$remote_dir" ]]
}

same_resources() {
  local image mount networks
  image=$(docker inspect --format '{{.Image}}' "$container") || return 1
  mount=$(docker inspect --format '{{range .Mounts}}{{if eq .Destination "/usr/share/elasticsearch/data"}}{{.Type}}|{{.Name}}|{{.Destination}}{{end}}{{end}}' "$container") || return 1
  networks=$(docker inspect --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}}{{"\n"}}{{end}}' "$container" | sed '/^$/d' | sort) || return 1
  [[ $image == "$previous_image_id" && $mount == "$previous_mount" && $networks == "$previous_networks" ]]
}

same_bindings() {
  local http transport
  http=$(docker inspect --format '{{with index .HostConfig.PortBindings "9200/tcp"}}{{range .}}{{.HostIp}}|{{.HostPort}}{{"\n"}}{{end}}{{end}}' "$container" | sed '/^$/d' | sort) || return 1
  transport=$(docker inspect --format '{{with index .HostConfig.PortBindings "9300/tcp"}}{{range .}}{{.HostIp}}|{{.HostPort}}{{"\n"}}{{end}}{{end}}' "$container" | sed '/^$/d' | sort) || return 1
  [[ $http == "$previous_http_bindings" && $transport == "$previous_transport_bindings" ]]
}

healthy() {
  [[ $(docker inspect --format '{{.State.Status}}|{{if .State.Health}}{{.State.Health.Status}}{{end}}' "$container") == 'running|healthy' ]]
}

wait_for_health() {
  local count
  for count in $(seq 1 60); do
    healthy && return 0
    sleep 3
  done
  return 1
}

loopback_ports() {
  local http transport http_port response
  http=$(docker port "$container" 9200/tcp) || return 1
  transport=$(docker port "$container" 9300/tcp) || return 1
  [[ $http =~ ^127\.0\.0\.1:([1-9][0-9]{0,4})$ ]] || return 1
  http_port=${BASH_REMATCH[1]}
  [[ $transport =~ ^127\.0\.0\.1:[1-9][0-9]{0,4}$ ]] || return 1
  response=$(curl --fail --silent --show-error --max-time 10 "http://127.0.0.1:$http_port/_cluster/health") || return 1
  grep -Eq '"status"[[:space:]]*:[[:space:]]*"(green|yellow)"' <<<"$response"
}

recreate() {
  local base_compose=$1
  local files=(-f "$base_compose" -f "$image_override")
  "${compose[@]}" "${files[@]}" config --quiet &&
    "${compose[@]}" "${files[@]}" up -d --pull never --no-deps --force-recreate "$service"
}

rollback() {
  if recreate "$rollback_compose" && wait_for_health && same_identity && same_resources && same_bindings; then
    echo 'ROLLOUT_RESULT=ROLLED_BACK'
    return 0
  fi
  echo 'ROLLOUT_RESULT=ROLLBACK_FAILED'
  return 1
}

if ! recreate compose.yml || ! wait_for_health || ! same_identity || ! same_resources || ! loopback_ports; then
  rollback || exit 2
  exit 1
fi

rm -f "$image_override"
echo 'ROLLOUT_RESULT=SUCCESS'
