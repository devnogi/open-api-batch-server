# 1차 설정 검증 기록

2026-10-21, Windows PowerShell, Amazon Corretto 21.0.12, 저장소 Gradle wrapper. 로컬 브랜치: codex/ai-workspace-phase1. commit/push/배포는 수행하지 않았다.

| 검사 | 결과 |
| --- | --- |
| 초기 .\gradlew.bat verify --continue --no-daemon | 실패: 기존 Java 180개 파일의 spotlessJavaCheck 포맷 위반, 41s |
| 포맷 정리 후 .\gradlew.bat verify --no-daemon | 통과: Spotless·113개 테스트·JaCoCo·bootJar, 52s |
| JUnit XML 합계 | 113개, 실패 0, 건너뜀 0 |
| node scripts/secret-scan.mjs | finding 0 (현재 트리의 기본 검사, 전체 이력 검사 아님) |
| Docker Compose config --no-env-resolution --quiet | 합성 .env.example 사용, 구문 검사 통과 |
| git diff --check | 통과 |

초기 검증에서는 기존 compileJava의 spotlessApply 자동 실행을 제거하면서 숨겨졌던 포맷 위반이 드러났다. --continue는 실패를 무시하지 않고 나머지 테스트·빌드 결과를 수집하기 위해 사용했다. 아래 포맷 정리 후에는 옵션 없이 전체 verify가 통과했다. CI 필수 검사는 앞으로 발생하는 실패도 차단해야 한다.

Docker 데몬 기동·이미지 빌드·전체 MSA 연동·운영 배포·실제 PR/원격 필수 검사 차단은 이 기록의 검증 범위 밖이다. --no-env-resolution은 서비스 env_file 로드를 생략하므로 실제 실행 전 .env.local 준비와 config 재검증이 필요하다.

## 포맷 정리 계획 (2026-10-21)

사용자가 README를 승인하고 남은 설정 마무리를 요청한 후, 기존 포맷 실패를 프로젝트의 `spotlessApply`로 정리한다. 동작·계약·의존성은 변경하지 않는다.

1. 적용 전 기존 JUnit XML 18개 suite의 113개 테스트, 실패·오류·건너뜀 0과 제품 소스의 기존 diff가 없음을 확인했다.
2. Java 21에서 `./gradlew spotlessApply --no-daemon`을 실행한다. formatter의 공백·줄바꿈·import 정리만 허용한다.
3. 실제 diff에서 포맷 이외 변경 여부를 확인하고 `./gradlew verify --no-daemon`, 기본 비밀값 검사와 `git diff --check`를 실행한다.
4. 최종 결과를 아래에 기록하며 commit/push/merge/배포는 이 작업에서 수행하지 않는다.

### 정리 결과

- `./gradlew spotlessApply --no-daemon`: 10초, exit 0. 기존 Java 180개 파일의 import 순서·공백만 정리했다.
- 180개 파일 모두 HEAD와 비교해 import 집합이 동일하고, import를 제외한 본문은 공백 제거 후 동일했다. 변경 통계는 298줄 추가·473줄 삭제이며 동작·계약 변경은 없다. 기존 설정 변경과 신규 계약 테스트는 보존했다.
- `./gradlew verify --no-daemon`: 52초, exit 0. `spotlessJavaCheck`, `spotlessMiscCheck`, `test`, `jacocoTestReport`, `bootJar` 완료. JUnit XML 18개 suite, 113개 테스트, 실패·오류·건너뜀 0.
- `node scripts/secret-scan.mjs`: finding 0. `git diff --check`: exit 0. Git의 기존 workflow LF/CRLF 경고는 검사 실패가 아니다.
- 컴파일의 기존 deprecated/unchecked 경고와 Gradle 9 호환성 경고는 남아 있다. Docker·라이브 PR 검증은 위 범위 제한을 유지한다.
