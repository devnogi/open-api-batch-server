import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
const pin = '698f7b25995156af3f2df7f17171e5c3f5e77d76';
const ci = read('.github/workflows/pr-ci.yml');
const prod = read('.github/workflows/push-cd-prod.yml');
const gradleProperties = read('gradle.properties');

test('production Batch and Elasticsearch ports are bound to loopback', () => {
  const compose = read('docker-compose-prod.yml');
  assert.match(compose, /ports:\s*\n\s*- "127\.0\.0\.1:\$\{SERVER_PORT\}:\$\{SERVER_PORT\}"/);
  assert.match(compose, /"127\.0\.0\.1:\$\{ELASTICSEARCH_PORT:-9200\}:9200"/);
  assert.match(compose, /"127\.0\.0\.1:\$\{ELASTICSEARCH_TRANSPORT_PORT:-9300\}:9300"/);
});

test('Elasticsearch loopback rollout preserves image and volume and recreates the dependency explicitly', () => {
  const guide = read('docs/CD_PIPELINE_GUIDE.md');
  assert.match(guide, /--no-deps spring-app/);
  assert.match(guide, /previous_image_id=\$\(docker inspect/);
  assert.match(guide, /previous_mounts=\$\(docker inspect/);
  assert.match(guide, /--project-directory "\$PWD"/);
  assert.match(guide, /--pull never --no-deps --force-recreate devnogi-elastic-search/);
  assert.match(guide, /-f "\$rollback_compose" -f "\$override"/);
  assert.match(guide, /docker compose down -v/);
});

test('PR caller targets the approved reusable Batch CI', () => {
  assert.match(ci, /pull_request:\s*\n\s*branches: \[dev, main\]/);
  assert.match(ci, /workflow_dispatch:/);
  assert.match(ci, new RegExp(`spring-ci\\.yml@${pin}`));
  assert.doesNotMatch(ci, /spring-prod\.yml|\bpush:/);
  assert.match(ci, /node --test scripts\/workflow-contract\.test\.mjs/);
  assert.match(ci, /ci:\s*\n\s*needs: contract/);
});

test('production caller only permits main push or manual dispatch', () => {
  assert.match(prod, /push:\s*\n\s*branches: \[main\]/);
  assert.match(prod, /workflow_dispatch:/);
  assert.match(prod, /if: github\.ref == 'refs\/heads\/main' && \(github\.event_name == 'push' \|\| github\.event_name == 'workflow_dispatch'\)/);
  assert.doesNotMatch(prod, /pull_request:/);
  assert.match(prod, new RegExp(`spring-prod\\.yml@${pin}`));
  assert.match(prod, /permissions:\s*\n\s*contents: write/);
  assert.match(prod, /concurrency:\s*\n\s*group: batch-production\s*\n\s*cancel-in-progress: false/);
  assert.match(prod, /node --test scripts\/workflow-contract\.test\.mjs/);
  assert.match(prod, /production:\s*\n\s*needs: contract/);
  assert.match(prod, /health-timeout: '300'/);
  assert.match(prod, /platform: linux\/amd64/);
  assert.match(prod, /secrets: inherit/);
  assert.match(prod, /deployment-target: prod2/);
  assert.match(gradleProperties, /^applicationVersion=1\.0\.2$/m);
});

test('callers agree on Batch inputs and Compose requires the image', () => {
  for (const workflow of [ci, prod]) {
    for (const line of ['service-profile: batch', 'service-name: batch',
      'dockerfile: Dockerfile', 'compose-file: docker-compose-prod.yml']) {
      assert.ok(workflow.includes(line), line);
    }
  }
  assert.match(read('docker-compose-prod.yml'), /spring-app:\s*\n\s*image: \$\{IMAGE_REF:\?IMAGE_REF is required\}/);
});

test('production Batch logs use the host collector and retain diagnostic cache', () => {
  const compose = read('docker-compose-prod.yml');
  const app = compose.split('  devnogi-elastic-search:')[0];
  for (const line of ['driver: "syslog"', 'syslog-address: "unixgram:///dev/log"',
    'syslog-format: "rfc5424micro"', 'tag: "memonogi-batch"', 'mode: "non-blocking"',
    'max-buffer-size: "4m"', 'cache-disabled: "false"', 'cache-max-size: "20m"',
    'cache-max-file: "5"', 'app-logs:/app/logs']) {
    assert.ok(app.includes(line), line);
  }
  assert.match(compose.split('  devnogi-elastic-search:')[1], /driver: "json-file"/);
});
