import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'https://www.memonogi.com';
const DURATION = __ENV.DURATION || '2m';
const RATE = parseInt(__ENV.RATE || '4', 10);
const PRE_ALLOCATED_VUS = parseInt(__ENV.PRE_ALLOCATED_VUS || '5', 10);
const MAX_VUS = parseInt(__ENV.MAX_VUS || '10', 10);
const REPORT_MD = __ENV.REPORT_MD || 'k6/results/manual/batch_read_api_performance_report.md';
const REPORT_JSON = __ENV.REPORT_JSON || 'k6/results/manual/batch_read_api_performance_summary.json';
const TARGET_MODE = __ENV.TARGET_MODE || 'proxy';

const TARGET_ENDPOINTS = [
  {
    slug: 'auction_history_search',
    group: 'Auction',
    label: '경매 거래 내역 검색',
    method: 'GET',
    path: '/api/auction-history/search?page=1&size=20&sortBy=dateAuctionBuy&direction=desc',
    localPath: '/auction-history/search?page=1&size=20&sortBy=dateAuctionBuy&direction=desc',
    source: '/oab/auction-history/search',
    cache: 's-maxage=300',
    weight: 14,
  },
  {
    slug: 'auction_realtime_search',
    group: 'Auction',
    label: '실시간 경매 검색',
    method: 'GET',
    path: '/api/auction-realtime/search?page=1&size=20&sortBy=dateAuctionExpire&direction=desc',
    localPath: '/auction-realtime/search?page=1&size=20&sortBy=dateAuctionExpire&direction=desc',
    source: '/oab/auction-realtime/search',
    cache: 's-maxage=60',
    weight: 12,
  },
  {
    slug: 'horn_bugle',
    group: 'Auction',
    label: '뿔피리 메시지 조회',
    method: 'GET',
    path: '/api/horn-bugle?page=1&size=20',
    localPath: '/horn-bugle?page=1&size=20',
    source: '/oab/horn-bugle',
    cache: 'revalidate=300',
    weight: 8,
  },
  {
    slug: 'item_categories',
    group: 'Metadata',
    label: '아이템 카테고리',
    method: 'GET',
    path: '/api/item-infos/categories',
    localPath: '/api/item-infos/categories',
    source: '/oab/api/item-infos/categories',
    cache: 'route proxy',
    weight: 3,
  },
  {
    slug: 'search_option',
    group: 'Metadata',
    label: '검색 옵션 메타데이터',
    method: 'GET',
    path: '/api/search-option',
    localPath: '/api/search-option',
    source: '/oab/api/search-option',
    cache: 's-maxage=1800',
    weight: 3,
  },
  {
    slug: 'enchant_fullnames',
    group: 'Metadata',
    label: '인챈트 풀네임 목록',
    method: 'GET',
    path: '/api/enchant-infos/fullnames',
    localPath: '/api/enchant-infos/fullnames',
    source: '/oab/api/enchant-infos/fullnames',
    cache: 'route proxy',
    weight: 3,
  },
  {
    slug: 'metalware_infos',
    group: 'Metadata',
    label: '세공 메타데이터',
    method: 'GET',
    path: '/api/metalware-infos',
    localPath: '/api/metalware-infos',
    source: '/oab/api/metalware-infos',
    cache: 'route proxy',
    weight: 3,
  },
  {
    slug: 'item_option_infos',
    group: 'Metadata',
    label: '아이템 옵션 메타데이터',
    method: 'GET',
    path: '/api/item-option-infos',
    localPath: '/api/v1/item-option-infos',
    source: '/oab/api/v1/item-option-infos',
    cache: 'route proxy',
    weight: 3,
  },
  {
    slug: 'item_detail',
    group: 'Metadata',
    label: '아이템 상세',
    method: 'GET',
    path: '/api/item-infos/detail?itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    localPath: '/api/item-infos/detail?itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    source: '/oab/api/item-infos/detail',
    cache: 's-maxage=300',
    weight: 3,
  },
  {
    slug: 'rankings_price',
    group: 'Rankings',
    label: '가격 랭킹',
    method: 'GET',
    path: '/api/rankings/price?type=today-highest&limit=20',
    localPath: '/rankings/price/today/highest?limit=20',
    source: '/oab/rankings/price/today/highest',
    cache: 'revalidate=300',
    weight: 5,
  },
  {
    slug: 'rankings_volume',
    group: 'Rankings',
    label: '거래량 랭킹',
    method: 'GET',
    path: '/api/rankings/volume?type=today&limit=20',
    localPath: '/rankings/volume/today/popular?limit=20',
    source: '/oab/rankings/volume/today/popular',
    cache: 'revalidate=300',
    weight: 5,
  },
  {
    slug: 'rankings_price_change',
    group: 'Rankings',
    label: '가격 변동 랭킹',
    method: 'GET',
    path: '/api/rankings/price-change?type=surge&limit=20',
    localPath: '/rankings/price-change/surge?limit=20',
    source: '/oab/rankings/price-change/surge',
    cache: 'revalidate=300',
    weight: 5,
  },
  {
    slug: 'rankings_all_time',
    group: 'Rankings',
    label: '전체 기간 최고가 랭킹',
    method: 'GET',
    path: '/api/rankings/all-time?type=highest-price&limit=20',
    localPath: '/rankings/all-time/highest-price?limit=20',
    source: '/oab/rankings/all-time/highest-price',
    cache: 'revalidate=300',
    weight: 5,
  },
  {
    slug: 'rankings_category',
    group: 'Rankings',
    label: '카테고리 랭킹',
    method: 'GET',
    path: '/api/rankings/category?type=top-priced&topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&limit=20',
    localPath: '/rankings/category/top-priced?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&limit=20',
    source: '/oab/rankings/category/top-priced',
    cache: 'revalidate=300',
    weight: 5,
  },
  {
    slug: 'statistics_daily_top_categories',
    group: 'Statistics',
    label: '일간 상위 카테고리 통계',
    method: 'GET',
    path: '/api/statistics/daily/top-categories?topCategory=%EA%B8%B0%ED%83%80',
    localPath: '/statistics/daily/top-categories?topCategory=%EA%B8%B0%ED%83%80',
    source: '/oab/statistics/daily/top-categories',
    cache: 'revalidate=300',
    weight: 4,
  },
  {
    slug: 'statistics_daily_subcategories',
    group: 'Statistics',
    label: '일간 서브카테고리 통계',
    method: 'GET',
    path: '/api/statistics/daily/subcategories?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80',
    localPath: '/statistics/daily/subcategories?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80',
    source: '/oab/statistics/daily/subcategories',
    cache: 'revalidate=300',
    weight: 4,
  },
  {
    slug: 'statistics_daily_items',
    group: 'Statistics',
    label: '일간 아이템 통계',
    method: 'GET',
    path: '/api/statistics/daily/items?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    localPath: '/statistics/daily/items?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    source: '/oab/statistics/daily/items',
    cache: 'revalidate=300',
    weight: 4,
  },
  {
    slug: 'statistics_weekly_top_categories',
    group: 'Statistics',
    label: '주간 상위 카테고리 통계',
    method: 'GET',
    path: '/api/statistics/weekly/top-categories?topCategory=%EA%B8%B0%ED%83%80',
    localPath: '/statistics/weekly/top-categories?topCategory=%EA%B8%B0%ED%83%80',
    source: '/oab/statistics/weekly/top-categories',
    cache: 'revalidate=300',
    weight: 4,
  },
  {
    slug: 'statistics_weekly_subcategories',
    group: 'Statistics',
    label: '주간 서브카테고리 통계',
    method: 'GET',
    path: '/api/statistics/weekly/subcategories?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80',
    localPath: '/statistics/weekly/subcategories?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80',
    source: '/oab/statistics/weekly/subcategories',
    cache: 'revalidate=300',
    weight: 4,
  },
  {
    slug: 'statistics_weekly_items',
    group: 'Statistics',
    label: '주간 아이템 통계',
    method: 'GET',
    path: '/api/statistics/weekly/items?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    localPath: '/statistics/weekly/items?topCategory=%EA%B8%B0%ED%83%80&subCategory=%EA%B8%B0%ED%83%80&itemName=%ED%96%A5%EA%B8%B0%EB%A1%9C%EC%9A%B4%20%EA%BF%80%20%EC%9A%B0%EC%9C%A0',
    source: '/oab/statistics/weekly/items',
    cache: 'revalidate=300',
    weight: 4,
  },
];

const weightedEndpoints = [];
for (const endpoint of TARGET_ENDPOINTS) {
  for (let i = 0; i < endpoint.weight; i += 1) {
    weightedEndpoints.push(endpoint);
  }
}

const endpointMetrics = {};
for (const endpoint of TARGET_ENDPOINTS) {
  endpointMetrics[endpoint.slug] = {
    duration: new Trend(`endpoint_${endpoint.slug}_duration_ms`, true),
    success: new Rate(`endpoint_${endpoint.slug}_success_rate`),
    requests: new Counter(`endpoint_${endpoint.slug}_requests`),
    status2xx: new Counter(`endpoint_${endpoint.slug}_status_2xx`),
    status4xx: new Counter(`endpoint_${endpoint.slug}_status_4xx`),
    status5xx: new Counter(`endpoint_${endpoint.slug}_status_5xx`),
    statusOther: new Counter(`endpoint_${endpoint.slug}_status_other`),
  };
}

export const options = {
  scenarios: {
    batch_readonly_portfolio: {
      executor: 'constant-arrival-rate',
      rate: RATE,
      timeUnit: '1s',
      duration: DURATION,
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
      exec: 'readonlyBatchApi',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    checks: ['rate>0.99'],
    http_req_duration: ['p(95)<2500', 'p(99)<4000'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  tags: {
    test_type: 'portfolio_readonly',
    target_domain: BASE_URL,
  },
};

function pickEndpoint() {
  return weightedEndpoints[Math.floor(Math.random() * weightedEndpoints.length)];
}

function metricValue(data, name, stat, fallback = 0) {
  const metric = data.metrics[name];
  const value = metric && metric.values ? metric.values[stat] : undefined;
  return Number.isFinite(value) ? value : fallback;
}

function fixed(value, digits = 1) {
  return Number.isFinite(value) ? value.toFixed(digits) : '-';
}

function pct(value, digits = 2) {
  return Number.isFinite(value) ? (value * 100).toFixed(digits) : '-';
}

function statusBucket(status) {
  if (status >= 200 && status < 300) return 'status2xx';
  if (status >= 400 && status < 500) return 'status4xx';
  if (status >= 500 && status < 600) return 'status5xx';
  return 'statusOther';
}

function requestPath(endpoint) {
  return TARGET_MODE === 'local-batch' ? endpoint.localPath : endpoint.path;
}

function callRouteDescription() {
  if (TARGET_MODE === 'local-batch') {
    return `로컬 batch 직접 경로 기준: ${BASE_URL}/* -> batch 애플리케이션`;
  }
  return `운영 사용자 경로 기준: ${BASE_URL}/api/* -> gateway -> batch(/oab/*)`;
}

function environmentNote() {
  if (TARGET_MODE === 'local-batch') {
    return `로컬 Docker batch 서버 대상 테스트이므로 ${RATE} RPS 수준의 동일한 보수적 부하로 측정했다. 운영 www 도메인의 Next.js API proxy/CDN/TLS 구간은 제외되고, 로컬 Docker 네트워크의 batch 애플리케이션/DB/Redis/Elasticsearch 구간이 포함된다.`;
  }
  return `운영 서버 대상 테스트이므로 서비스 영향도를 낮추기 위해 ${RATE} RPS 수준의 보수적 부하로 측정했다. 직접 batch 도메인이 아니라 실제 사용자 진입점인 www 도메인의 Next.js API proxy를 통해 측정했으므로 CDN/TLS/Next route handler/gateway/batch 구간이 함께 포함된다.`;
}

function targetSummaryLabel() {
  return TARGET_MODE === 'local-batch' ? '로컬 Docker batch 서버 기준' : '운영 도메인 기준';
}

function cacheCaution() {
  if (TARGET_MODE === 'local-batch') {
    return '- 운영 www 도메인의 Next.js route handler 캐시, CDN, TLS, gateway 구간은 포함하지 않은 batch 애플리케이션 직접 측정 결과다.';
  }
  return '- Next.js route handler 캐시가 적용된 API가 포함되어 있어, 순수 batch 애플리케이션/DB 단독 처리시간과는 다를 수 있다.';
}

function kstTimestamp() {
  const now = new Date(Date.now() + 9 * 60 * 60 * 1000);
  return now.toISOString().replace('T', ' ').replace(/\.\d{3}Z$/, ' KST');
}

function buildEndpointRows(data) {
  return TARGET_ENDPOINTS.map((endpoint) => {
    const prefix = `endpoint_${endpoint.slug}`;
    const requests = metricValue(data, `${prefix}_requests`, 'count');
    return {
      ...endpoint,
      requests,
      successRate: requests > 0 ? metricValue(data, `${prefix}_success_rate`, 'rate') : null,
      avg: metricValue(data, `${prefix}_duration_ms`, 'avg'),
      med: metricValue(data, `${prefix}_duration_ms`, 'med'),
      p90: metricValue(data, `${prefix}_duration_ms`, 'p(90)'),
      p95: metricValue(data, `${prefix}_duration_ms`, 'p(95)'),
      p99: metricValue(data, `${prefix}_duration_ms`, 'p(99)'),
      min: metricValue(data, `${prefix}_duration_ms`, 'min'),
      max: metricValue(data, `${prefix}_duration_ms`, 'max'),
      status2xx: metricValue(data, `${prefix}_status_2xx`, 'count'),
      status4xx: metricValue(data, `${prefix}_status_4xx`, 'count'),
      status5xx: metricValue(data, `${prefix}_status_5xx`, 'count'),
      statusOther: metricValue(data, `${prefix}_status_other`, 'count'),
    };
  });
}

function buildGroupRows(endpointRows) {
  const groups = {};
  for (const row of endpointRows) {
    if (!groups[row.group]) {
      groups[row.group] = {
        group: row.group,
        endpoints: 0,
        requests: 0,
        weightedAvgSum: 0,
        p95Max: 0,
        successCount: 0,
      };
    }
    groups[row.group].endpoints += 1;
    groups[row.group].requests += row.requests;
    groups[row.group].weightedAvgSum += row.avg * row.requests;
    groups[row.group].p95Max = Math.max(groups[row.group].p95Max, row.p95);
    groups[row.group].successCount += row.requests * (row.successRate || 0);
  }

  return Object.values(groups).map((group) => ({
    ...group,
    avg: group.requests > 0 ? group.weightedAvgSum / group.requests : 0,
    successRate: group.requests > 0 ? group.successCount / group.requests : 0,
  }));
}

function buildMarkdown(data) {
  const endpointRows = buildEndpointRows(data);
  const groupRows = buildGroupRows(endpointRows);
  const totalRequests = metricValue(data, 'http_reqs', 'count');
  const failedRate = metricValue(data, 'http_req_failed', 'rate');
  const checksRate = metricValue(data, 'checks', 'rate');
  const duration = data.metrics.http_req_duration.values;
  const receivedBytes = metricValue(data, 'data_received', 'count');
  const sentBytes = metricValue(data, 'data_sent', 'count');
  const droppedIterations = metricValue(data, 'dropped_iterations', 'count');
  const testRunMs = data.state && data.state.testRunDurationMs ? data.state.testRunDurationMs : 0;
  const measuredSeconds = testRunMs > 0 ? testRunMs / 1000 : null;
  const achievedRps = measuredSeconds ? totalRequests / measuredSeconds : RATE;
  const successfulRequests = Math.round(totalRequests * (1 - failedRate));
  const failedRequests = totalRequests - successfulRequests;

  const endpointTable = endpointRows
    .map((row) => `| ${row.group} | ${row.label} | ${row.method} ${requestPath(row)} | ${row.requests} | ${pct(row.successRate)}% | ${fixed(row.avg)} | ${fixed(row.med)} | ${fixed(row.p90)} | ${fixed(row.p95)} | ${fixed(row.p99)} | ${fixed(row.max)} | ${row.status2xx}/${row.status4xx}/${row.status5xx}/${row.statusOther} |`)
    .join('\n');

  const groupTable = groupRows
    .map((row) => `| ${row.group} | ${row.endpoints} | ${row.requests} | ${pct(row.successRate)}% | ${fixed(row.avg)} | ${fixed(row.p95Max)} |`)
    .join('\n');

  const targetTable = TARGET_ENDPOINTS
    .map((endpoint) => `| ${endpoint.group} | ${endpoint.label} | ${endpoint.method} ${requestPath(endpoint)} | ${endpoint.source} | ${endpoint.cache} |`)
    .join('\n');

  return `# DEVNOGI Batch 조회 API 성능 테스트 결과

## 1. 테스트 개요

| 항목 | 내용 |
|---|---|
| 측정 일시 | ${kstTimestamp()} |
| 대상 도메인 | ${BASE_URL} |
| 측정 도구 | K6 v1.2.3 |
| 테스트 대상 | batch 서버 조회 API ${TARGET_ENDPOINTS.length}종 |
| 호출 경로 | ${callRouteDescription()} |
| 제외 범위 | 로그인/인증 필요 API, 관리자 API, POST/PUT/PATCH/DELETE, batch sync/write API |
| 실행 모델 | constant-arrival-rate |
| 목표 부하 | ${RATE} req/s, ${DURATION}, preAllocatedVUs=${PRE_ALLOCATED_VUS}, maxVUs=${MAX_VUS} |
| 실제 처리량 | ${fixed(achievedRps, 2)} req/s |
| 총 요청 수 | ${totalRequests}건 |
| Dropped iterations | ${droppedIterations}건 |
| 성공 요청 수 | ${successfulRequests}건 |
| 실패 요청 수 | ${failedRequests}건 |
| 응답 데이터 수신량 | ${(receivedBytes / 1024 / 1024).toFixed(2)} MiB |
| 요청 데이터 송신량 | ${(sentBytes / 1024 / 1024).toFixed(2)} MiB |

> ${environmentNote()}

## 2. 전체 결과 요약

| 지표 | 결과 |
|---|---:|
| HTTP 실패율 | ${pct(failedRate)}% |
| K6 check 성공률 | ${pct(checksRate)}% |
| 평균 응답시간 | ${fixed(duration.avg)} ms |
| 중앙값 응답시간 | ${fixed(duration.med)} ms |
| p90 응답시간 | ${fixed(duration['p(90)'])} ms |
| p95 응답시간 | ${fixed(duration['p(95)'])} ms |
| p99 응답시간 | ${fixed(duration['p(99)'])} ms |
| 최소 응답시간 | ${fixed(duration.min)} ms |
| 최대 응답시간 | ${fixed(duration.max)} ms |

## 3. API 그룹별 결과

| 그룹 | API 수 | 요청 수 | 성공률 | 가중 평균 응답시간(ms) | 그룹 내 최대 p95(ms) |
|---|---:|---:|---:|---:|---:|
${groupTable}

## 4. API별 상세 결과

| 그룹 | API | 경로 | 요청 수 | 성공률 | avg(ms) | med(ms) | p90(ms) | p95(ms) | p99(ms) | max(ms) | 상태코드 2xx/4xx/5xx/기타 |
|---|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
${endpointTable}

## 5. 테스트 대상 API 목록

| 그룹 | 기능 | 호출 경로 | batch 원천 경로 | 캐시/비고 |
|---|---|---|---|---|
${targetTable}

## 6. 포트폴리오 기재용 요약

- ${targetSummaryLabel()} batch 조회 API ${TARGET_ENDPOINTS.length}종에 대해 K6 성능 테스트를 수행했다.
- 테스트는 조회 전용 GET API만 대상으로 구성했고, 쓰기성 batch sync/API 및 인증 필요 API는 제외했다.
- ${DURATION} 동안 ${RATE} req/s의 고정 도착률로 총 ${totalRequests}건을 호출했으며, HTTP 성공률은 ${pct(1 - failedRate)}%, K6 check 성공률은 ${pct(checksRate)}%로 측정됐다.
- 전체 응답시간은 평균 ${fixed(duration.avg)}ms, p95 ${fixed(duration['p(95)'])}ms, p99 ${fixed(duration['p(99)'])}ms로 측정됐다.
- 카테고리/검색 옵션/랭킹/통계/경매 검색 등 실제 사용자 조회 흐름을 반영해 API 그룹별 응답시간과 tail latency를 분리 산출했다.

## 7. 재현 명령

\`\`\`bash
BASE_URL=${BASE_URL} \\
TARGET_MODE=${TARGET_MODE} \\
RATE=${RATE} DURATION=${DURATION} \\
PRE_ALLOCATED_VUS=${PRE_ALLOCATED_VUS} MAX_VUS=${MAX_VUS} \\
REPORT_MD=${REPORT_MD} \\
REPORT_JSON=${REPORT_JSON} \\
k6 run performance/k6/scenarios/batch_readonly_portfolio.js
\`\`\`

## 8. 해석 시 주의사항

- 본 결과는 ${kstTimestamp().slice(0, 10)} KST에 로컬 Mac에서 ${BASE_URL} 대상으로 측정한 값이다.
- 서버 내부 CPU, 메모리, DB connection pool, Redis hit ratio, JVM GC, DB slow query 같은 내부 지표는 포함하지 않았다.
${cacheCaution()}
- JMeter는 로컬에 설치되어 있지 않아 이번 산출물은 K6 기준으로 작성했다.
`;
}

export function readonlyBatchApi() {
  const endpoint = pickEndpoint();
  const metrics = endpointMetrics[endpoint.slug];
  const url = `${BASE_URL}${requestPath(endpoint)}`;
  const res = http.get(url, {
    headers: {
      Accept: 'application/json',
      'User-Agent': 'k6-devnogi-portfolio-readonly/1.0',
    },
    tags: {
      api_group: endpoint.group,
      endpoint_slug: endpoint.slug,
      name: endpoint.label,
    },
  });

  const ok = check(res, {
    [`${endpoint.slug} status is 200`]: (r) => r.status === 200,
    [`${endpoint.slug} body is not empty`]: (r) => !!r.body && r.body.length > 0,
  });

  metrics.duration.add(res.timings.duration);
  metrics.success.add(ok);
  metrics.requests.add(1);
  metrics[statusBucket(res.status)].add(1);
}

export function handleSummary(data) {
  const markdown = buildMarkdown(data);
  const endpointRows = buildEndpointRows(data);
  const json = JSON.stringify(
    {
      generatedAtKst: kstTimestamp(),
      baseUrl: BASE_URL,
      targetMode: TARGET_MODE,
      duration: DURATION,
      rate: RATE,
      targetEndpointCount: TARGET_ENDPOINTS.length,
      endpoints: endpointRows,
      summaryMetrics: data.metrics,
    },
    null,
    2,
  );

  return {
    stdout: markdown,
    [REPORT_MD]: markdown,
    [REPORT_JSON]: json,
  };
}
