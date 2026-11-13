# 아이템 SEO 읽기 계약

소비자: devnogi-react의 아이템 SSR 페이지와 sitemap. 아래 응답 및 `contracts/item-*-response.example.json`은 `ApiResponse.data`이다. 실제 wire envelope는 `{success,code,message,data,timestamp}`이며 성공 code는 `COMMON_SUCCESS`다. 두 API는 익명 GET이며 `Cache-Control: no-store`로 소비자 재검증 시 원본 상태를 읽는다. Gateway 외부 경로는 `/oab` 접두사를 붙인다.

## 단일 아이템

`GET /api/item-infos/seo?name=...&topCategory=...&subCategory=...`

세 필드는 필수이며 공백만 허용하지 않는다. name 최대 255자, 두 category 최대 25자로 현재 카탈로그 컬럼 제한과 같다. 정확한 세 필드로 조회하고 저장된 카탈로그 identity를 `item`에 반환한다. 없는 카탈로그는 404, 잘못된 입력은 400, 저장소 오류는 5xx다.

`data = {item: ItemInfoResponse, seoEligible: boolean, lastTrade: LastTrade | null}`. description, acquisitionMethod, repair, storeSalesPrice 중 하나에 공백 이외 문자가 있거나 보존된 일간/주간 통계가 하나라도 있으면 seoEligible이다. 차트의 30/120일 표시 범위로 이 자격을 제한하지 않는다. 정보와 통계가 모두 없으면 카탈로그는 200이고 seoEligible=false다.

LastTrade 필드는 `periodType`(DAILY/WEEKLY), `periodStart`, `periodEnd`(ISO 날짜), `minPrice`, `maxPrice`, `avgPrice`, `totalQuantity`, `totalVolume`, `updatedAt`이다. 최신 periodStart를 선택하고 같은 날짜면 DAILY를 선택한다. DAILY 종료일은 시작일과 같고 WEEKLY 종료일은 시작일+6일이다. avgPrice는 총 거래 금액/수량을 소수점 둘째 자리 HALF_UP으로 계산한다(수량 0이면 저장 평균값). totalVolume은 금액, totalQuantity는 아이템 수량이며 거래 건수가 아니다. updatedAt은 nullable ISO local datetime으로 원본 LocalDateTime을 보존한다. 원본에 offset/zone 정보가 없으므로 UTC 또는 KST라고 단정하거나 임의의 Z를 붙이지 않는다. 사용자에게 거래 시점을 표시할 때는 periodStart/periodEnd를 사용한다.

## Sitemap

`GET /api/item-infos/sitemap?cursor=...&limit=1000`

limit 생략 시 1000, 범위 1..1000. cursor 생략 시 처음부터 조회한다. cursor는 UTF-8 JSON `[name,topCategory,subCategory]`의 padding 없는 base64url이고, 프론트 itemKey와 별개다. 빈 값, 잘못된 base64/UTF-8/JSON, 필드 수·타입·길이 오류는 400이다. `name,topCategory,subCategory ASC`로 커서보다 큰 항목만 반환한다. 마지막 페이지 nextCursor=null, hasNext=false. 다음 페이지가 있을 때만 마지막 반환 항목의 커서를 제공한다.

`items`는 name/topCategory/subCategory만 포함한다. 단일 API와 동일한 DB eligibility predicate와 EXISTS를 사용한다. `(name,top_category,sub_category)` 보조 인덱스로 identity 후보를 최대 1000개 읽고, `(이전 커서, 후보 마지막 identity]` 범위에만 eligibility를 적용한다. 응답 limit+1개를 확보하거나 카탈로그가 끝날 때까지 진행하므로 자격이 없는 후보 묶음에서도 다음 데이터를 누락하지 않는다. 공개 nextCursor는 마지막 반환 항목 기준이며 내부 후보 마지막 위치를 노출하지 않는다. 카탈로그/통계는 운영 DB collation을 따른다. 별도 ID나 수집 파이프라인 변경은 없다.

V34는 기존 PK를 유지하면서 정렬에 맞는 보조 인덱스를 추가한다. 기존 인덱스 존재 여부를 확인하므로 재실행할 수 있다. 인덱스만 추가하면 첫 페이지에서 옵티마이저가 전체 스캔을 유지할 수 있어 후보 범위를 먼저 제한한다. 후보 묶음당 두 조회를 사용하며, 자격이 거의 없는 마지막 페이지는 여러 묶음을 읽을 수 있다. 합성 MySQL의 후보 covering-index 및 eligibility range 계획을 테스트하지만 운영 DB의 실행 시간이나 인덱스 생성 시간을 보장하지 않는다.

## 검증

`gradlew.bat test --tests '*ItemSeo*' --no-daemon`은 입력·오류·커서·기간·가중 평균과 MySQL 8의 실제 projection/EXISTS를 검증한다. Docker가 있으면 합성 MySQL 컨테이너에 기존 Flyway를 적용하고 오래된 통계·동명 다른 category·EXPLAIN을 검증한다. Docker가 없으면 해당 통합 테스트가 skip되므로 실행 증거에서 구분해야 한다. 전체 필수 검증은 `gradlew.bat verify --no-daemon`이다.
