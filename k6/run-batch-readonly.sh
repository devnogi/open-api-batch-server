#!/usr/bin/env bash
# DEVNOGI batch read-only API K6 runner.
#
# Usage:
#   cd open-api-batch-server
#   ./k6/run-batch-readonly.sh local smoke
#   ./k6/run-batch-readonly.sh local portfolio
#   ./k6/run-batch-readonly.sh prod portfolio
#   BASE_URL=http://localhost:8092 TARGET_MODE=local-batch ./k6/run-batch-readonly.sh custom smoke

set -euo pipefail

BATCH_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SCRIPT_PATH="${BATCH_ROOT}/k6/scenarios/batch_readonly_portfolio.js"

TARGET="${1:-local}"
PROFILE="${2:-portfolio}"
RUN_ID="${RUN_ID:-$(date +%Y%m%d_%H%M%S)}"
OUT_ROOT="${OUT_ROOT:-${BATCH_ROOT}/k6/results}"

case "${TARGET}" in
  local)
    BASE_URL="${BASE_URL:-http://localhost:8090}"
    TARGET_MODE="${TARGET_MODE:-local-batch}"
    ;;
  prod|production)
    TARGET="prod"
    BASE_URL="${BASE_URL:-https://www.memonogi.com}"
    TARGET_MODE="${TARGET_MODE:-proxy}"
    ;;
  custom)
    if [ -z "${BASE_URL:-}" ]; then
      echo "[ERR] custom target requires BASE_URL." >&2
      exit 1
    fi
    TARGET_MODE="${TARGET_MODE:-local-batch}"
    ;;
  *)
    echo "[ERR] Unknown target: ${TARGET}" >&2
    echo "      Use one of: local | prod | custom" >&2
    exit 1
    ;;
esac

case "${PROFILE}" in
  smoke)
    RATE="${RATE:-2}"
    DURATION="${DURATION:-20s}"
    PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS:-3}"
    MAX_VUS="${MAX_VUS:-6}"
    ;;
  portfolio)
    RATE="${RATE:-4}"
    DURATION="${DURATION:-2m}"
    PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS:-5}"
    MAX_VUS="${MAX_VUS:-10}"
    ;;
  load)
    RATE="${RATE:-10}"
    DURATION="${DURATION:-5m}"
    PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS:-15}"
    MAX_VUS="${MAX_VUS:-30}"
    ;;
  *)
    echo "[ERR] Unknown profile: ${PROFILE}" >&2
    echo "      Use one of: smoke | portfolio | load" >&2
    exit 1
    ;;
esac

if [ ! -f "${SCRIPT_PATH}" ]; then
  echo "[ERR] K6 scenario not found: ${SCRIPT_PATH}" >&2
  exit 1
fi

RESULT_DIR="${OUT_ROOT}/${TARGET}/${RUN_ID}"
REPORT_BASENAME="batch_readonly_${TARGET}_${PROFILE}_${RUN_ID}"
REPORT_MD="${RESULT_DIR}/${REPORT_BASENAME}.md"
REPORT_JSON="${RESULT_DIR}/${REPORT_BASENAME}.json"
CONSOLE_LOG="${RESULT_DIR}/${REPORT_BASENAME}.log"
RUN_META="${RESULT_DIR}/${REPORT_BASENAME}.env"

mkdir -p "${RESULT_DIR}"

cat > "${RUN_META}" <<EOF
RUN_ID=${RUN_ID}
TARGET=${TARGET}
PROFILE=${PROFILE}
BASE_URL=${BASE_URL}
TARGET_MODE=${TARGET_MODE}
RATE=${RATE}
DURATION=${DURATION}
PRE_ALLOCATED_VUS=${PRE_ALLOCATED_VUS}
MAX_VUS=${MAX_VUS}
REPORT_MD=${REPORT_MD}
REPORT_JSON=${REPORT_JSON}
CONSOLE_LOG=${CONSOLE_LOG}
EOF

echo "[INFO] DEVNOGI batch read-only K6 test"
echo "[INFO] target       : ${TARGET}"
echo "[INFO] profile      : ${PROFILE}"
echo "[INFO] base url     : ${BASE_URL}"
echo "[INFO] target mode  : ${TARGET_MODE}"
echo "[INFO] load         : ${RATE} req/s, ${DURATION}, preVUs=${PRE_ALLOCATED_VUS}, maxVUs=${MAX_VUS}"
echo "[INFO] result dir   : ${RESULT_DIR}"
echo "[INFO] markdown     : ${REPORT_MD}"
echo "[INFO] json         : ${REPORT_JSON}"
echo "[INFO] console log  : ${CONSOLE_LOG}"

if [ "${DRY_RUN:-0}" = "1" ]; then
  echo "[INFO] DRY_RUN=1, skip k6 execution."
  echo "[INFO] saved metadata: ${RUN_META}"
  exit 0
fi

if ! command -v k6 >/dev/null 2>&1; then
  echo "[ERR] k6 is not installed. Install with: brew install k6" >&2
  exit 1
fi

set +e
BASE_URL="${BASE_URL}" \
TARGET_MODE="${TARGET_MODE}" \
RATE="${RATE}" \
DURATION="${DURATION}" \
PRE_ALLOCATED_VUS="${PRE_ALLOCATED_VUS}" \
MAX_VUS="${MAX_VUS}" \
REPORT_MD="${REPORT_MD}" \
REPORT_JSON="${REPORT_JSON}" \
k6 run "${SCRIPT_PATH}" 2>&1 | tee "${CONSOLE_LOG}"
exit_code=${PIPESTATUS[0]}
set -e

echo "[INFO] saved metadata: ${RUN_META}"

if [ "${exit_code}" -eq 0 ]; then
  echo "[OK] K6 test completed."
else
  echo "[WARN] K6 test finished with exit code ${exit_code}." >&2
fi

exit "${exit_code}"
