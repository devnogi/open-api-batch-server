import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
const pin = '5e016f1fec66dd7e0bb752ec77d822b80940ac0c';
const ci = read('.github/workflows/pr-ci.yml');
const prod = read('.github/workflows/push-cd-prod.yml');
const gradleProperties = read('gradle.properties');

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
  assert.match(gradleProperties, /^applicationVersion=1\.0\.0$/m);
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
