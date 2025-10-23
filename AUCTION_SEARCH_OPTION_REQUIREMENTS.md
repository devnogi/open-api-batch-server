# Auction Item Option 검색 기능 구현 요구사항 명세서

> 작성일: 2025-10-20
> 프로젝트: open-api-batch-server
> 기능: 경매 거래내역 아이템 옵션 검색 및 검색 조건 메타데이터 제공 API

---

## 📋 개요

경매 거래내역(AuctionHistory) 검색 시 아이템 옵션(AuctionItemOption)을 조건으로 검색할 수 있는 기능을 구현하고, 프론트엔드가 동적으로 검색 필터 UI를 구성할 수 있도록 검색 조건 메타데이터를 제공하는 API를 개발합니다.

---

## 🎯 구현 목표

### 1. 검색 조건 메타데이터 제공 API
- DB 테이블에 검색 조건 정보 저장 (코드 하드코딩 ❌)
- REST API로 검색 조건 메타데이터 제공
- 프론트엔드에서 동적 검색 필터 UI 구성 가능

### 2. Auction Item Option 기반 검색 기능
- 경매 거래내역 검색 시 아이템 옵션을 조건으로 추가
- QueryDSL을 활용한 동적 쿼리 구현
- 기존 검색 조건과 결합 가능

---

## 💾 DB 테이블 설계

### 테이블명: `auction_search_option_metadata`

**DDL (MySQL 8):**
```sql
CREATE TABLE `auction_search_option_metadata` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `search_option_name` VARCHAR(100) NOT NULL COMMENT '검색 옵션명 (한글)',
    `search_condition_json` JSON NOT NULL COMMENT '검색 조건 (파라미터명:타입)',
    `display_order` INT NOT NULL UNIQUE COMMENT '정렬 순서 (고유값)',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`id`),
    INDEX `idx_display_order` (`display_order`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='경매 검색 옵션 메타데이터';
```

**JSON 필드 구조 (search_condition_json):**
```json
{
  "Balance": {
    "type": "tinyint",
    "required": false
  },
  "BalanceStandard": {
    "type": "string",
    "allowedValues": ["UP", "DOWN"],
    "required": false
  }
}
```

**초기 데이터 예시:**
| id | search_option_name | search_condition_json | display_order | is_active |
|----|-------------------|----------------------|--------------|-----------|
| 1 | 밸런스 | `{"Balance":{"type":"tinyint","required":false},"BalanceStandard":{"type":"string","allowedValues":["UP","DOWN"],"required":false}}` | 1 | true |
| 2 | 크리티컬 | `{"Critical":{"type":"tinyint","required":false},"CriticalStandard":{"type":"string","allowedValues":["UP","DOWN"],"required":false}}` | 2 | true |

---

## 🔄 Flyway Migration

### V 스크립트 (Schema)
**파일명:** `V{next_version}__create_auction_search_option_metadata.sql`
- 테이블 생성 DDL

### R 스크립트 (Repeatable - 초기 데이터)
**파일명:** `R__insert_auction_search_option_metadata.sql`
- 검색 조건 메타데이터 초기 데이터 INSERT
- 대상 항목 (auction_history_search_param.txt 1-18번):
  1. 밸런스 (Balance, BalanceStandard)
  2. 크리티컬 (Critical, CriticalStandard)
  3. 방어력 (Defense, DefenseStandard)
  4. 에르그 (ErgFrom, ErgTo)
  5. 에르그 등급 (ErgRank)
  6. 마법 방어력 (MagicDefense, MagicDefenseStandard)
  7. 마법 보호 (MagicProtect, MagicProtectStandard)
  8. 최대 공격력 (MaxAttackFrom, MaxAttackTo)
  9. 최대 내구력 (MaximumDurability, MaximumDurabilityStandard)
  10. 최대 부상률 (MaxInjuryRateFrom, MaxInjuryRateTo)
  11. 가격 (PriceFrom, PriceTo) - **제외 (이미 PriceSearchRequest 존재)**
  12. 숙련도 (Proficiency, ProficiencyStandard)
  13. 보호 (Protect, ProtectStandard)
  14. 남은 거래 횟수 (RemainingTransactionCount, RemainingTransactionCountStandard)
  15. 남은 전용 해제 가능 횟수 (RemainingUnsealCount, RemainingUnsealCountStandard)
  16. 남은 사용 횟수 (RemainingUseCount, RemainingUseCountStandard)
  17. 착용 제한 (WearingRestrictions)

**참고:** 19-37번 미정의 항목은 **제외**

---

## 🌐 API 명세

### GET /api/search-option

**설명:** 경매 검색 옵션 메타데이터 조회

**Request:**
- Method: `GET`
- Path: `/api/search-option`
- Parameters: 없음

**Response:**
```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "검색 옵션 조회 성공",
  "data": [
    {
      "id": 1,
      "searchOptionName": "밸런스",
      "searchCondition": {
        "Balance": {
          "type": "tinyint",
          "required": false
        },
        "BalanceStandard": {
          "type": "string",
          "allowedValues": ["UP", "DOWN"],
          "required": false
        }
      },
      "displayOrder": 1
    },
    {
      "id": 2,
      "searchOptionName": "크리티컬",
      "searchCondition": {
        "Critical": {
          "type": "tinyint",
          "required": false
        },
        "CriticalStandard": {
          "type": "string",
          "allowedValues": ["UP", "DOWN"],
          "required": false
        }
      },
      "displayOrder": 2
    }
  ],
  "timestamp": "2025-10-20T12:00:00Z"
}
```

**프론트엔드 UI 구현 가이드:**
- `*Standard` 필드 (UP/DOWN): 텍스트가 아닌 **화살표 아이콘**으로 표시
  - UP: ↑ (위쪽 화살표) - "이상" 조건
  - DOWN: ↓ (아래쪽 화살표) - "이하" 조건
  - 클릭 시 UP ↔ DOWN 토글

---

## 🏗️ 구현 레이어 구조

### Clean Architecture 패턴 적용

```
auctionsearchoption/
├── application/
│   └── service/
│       └── AuctionSearchOptionService.java
├── domain/
│   ├── entity/
│   │   └── AuctionSearchOptionMetadata.java
│   └── repository/
│       └── AuctionSearchOptionRepositoryPort.java  // 인터페이스
├── infrastructure/
│   └── persistence/
│       ├── AuctionSearchOptionJpaRepository.java
│       └── AuctionSearchOptionRepositoryPortImpl.java  // Port 구현체
└── interfaces/
    └── rest/
        ├── AuctionSearchOptionController.java
        └── dto/
            └── response/
                └── SearchOptionMetadataResponse.java
```

**Repository 패턴:**
- Port (인터페이스): `AuctionSearchOptionRepositoryPort`
- Port 구현체: `AuctionSearchOptionRepositoryPortImpl`
- JPA Repository: `AuctionSearchOptionJpaRepository` (Spring Data JPA)

---

## 📦 DTO 설계

### Request DTO

**기존:** `AuctionHistorySearchRequest`
```java
public record AuctionHistorySearchRequest(
    String itemName,
    String itemTopCategory,
    String itemSubCategory,
    String auction_price_per_unit,
    String date_auction_buy,
    PriceSearchRequest priceSearchRequest,  // 변경됨 (구 PricePerUnitSearchRequest)
    ItemOptionSearchRequest itemOptionSearchRequest  // 새로 추가
) {}
```

**새로 추가:** 개별 옵션 검색 Request DTO들 (Record로 구현)

각 옵션별로 별도 Record 생성:

```java
// 1. 단일 값 + Standard (UP/DOWN) 패턴
public record BalanceSearchRequest(
    Integer balance,
    String balanceStandard  // "UP" | "DOWN"
) {}

public record CriticalSearchRequest(
    Integer critical,
    String criticalStandard
) {}

public record DefenseSearchRequest(
    Integer defense,
    String defenseStandard
) {}

public record MagicDefenseSearchRequest(
    Integer magicDefense,
    String magicDefenseStandard
) {}

public record MagicProtectSearchRequest(
    Integer magicProtect,
    String magicProtectStandard
) {}

public record MaximumDurabilitySearchRequest(
    Integer maximumDurability,
    String maximumDurabilityStandard
) {}

public record ProficiencySearchRequest(
    Integer proficiency,
    String proficiencyStandard
) {}

public record ProtectSearchRequest(
    Integer protect,
    String protectStandard
) {}

public record RemainingTransactionCountSearchRequest(
    Integer remainingTransactionCount,
    String remainingTransactionCountStandard
) {}

public record RemainingUnsealCountSearchRequest(
    Integer remainingUnsealCount,
    String remainingUnsealCountStandard
) {}

public record RemainingUseCountSearchRequest(
    Integer remainingUseCount,
    String remainingUseCountStandard
) {}

// 2. 범위 검색 (From/To) 패턴
public record ErgSearchRequest(
    Integer ergFrom,
    Integer ergTo
) {}

public record MaxAttackSearchRequest(
    Integer maxAttackFrom,
    Integer maxAttackTo
) {}

public record MaxInjuryRateSearchRequest(
    Integer maxInjuryRateFrom,
    Integer maxInjuryRateTo
) {}

// PriceSearchRequest는 이미 존재 (기존 PricePerUnitSearchRequest에서 이름 변경)

// 3. Enum 값 패턴
public record ErgRankSearchRequest(
    String ergRank  // "S등급" | "A등급" | "B등급"
) {}

// 4. 문자열 검색 패턴
public record WearingRestrictionsSearchRequest(
    String wearingRestrictions
) {}
```

**통합:** `ItemOptionSearchRequest`
```java
public record ItemOptionSearchRequest(
    BalanceSearchRequest balanceSearch,
    CriticalSearchRequest criticalSearch,
    DefenseSearchRequest defenseSearch,
    ErgSearchRequest ergSearch,
    ErgRankSearchRequest ergRankSearch,
    MagicDefenseSearchRequest magicDefenseSearch,
    MagicProtectSearchRequest magicProtectSearch,
    MaxAttackSearchRequest maxAttackSearch,
    MaximumDurabilitySearchRequest maximumDurabilitySearch,
    MaxInjuryRateSearchRequest maxInjuryRateSearch,
    ProficiencySearchRequest proficiencySearch,
    ProtectSearchRequest protectSearch,
    RemainingTransactionCountSearchRequest remainingTransactionCountSearch,
    RemainingUnsealCountSearchRequest remainingUnsealCountSearch,
    RemainingUseCountSearchRequest remainingUseCountSearch,
    WearingRestrictionsSearchRequest wearingRestrictionsSearch
) {}
```

**분리 이유:** 일부 검색 타입은 `List<Request>` 형태로 받아야 할 수도 있기 때문

### Response DTO

```java
public record SearchOptionMetadataResponse(
    Long id,
    String searchOptionName,
    Map<String, FieldMetadata> searchCondition,  // JSON 파싱 결과
    Integer displayOrder
) {}

public record FieldMetadata(
    String type,
    Boolean required,
    List<String> allowedValues  // Optional, Enum 타입일 경우만
) {}
```

---

## 🔍 QueryDSL 검색 로직

### 검색 요구사항 명확화

**검색 흐름:**
1. 특정 옵션 조건 + 거래내역 조건으로 검색
2. 조건을 만족하는 경매장 거래 내역 찾기
3. ⭐ **해당 거래내역의 모든 옵션을 함께 조회** (조건 만족 여부 무관)

**예시:**
- 조건: "공격 +10 이상 OR 밸런스 +5 이상"
- 매칭된 거래내역: "페러시우스 타이탄 블레이드"
  - 옵션 1: 공격 +12 ✅ (조건 만족)
  - 옵션 2: 밸런스 +3 (조건 불만족이지만 반환)
  - 옵션 3: 크리티컬 +15 (조건 불만족이지만 반환)

**반환:** 거래내역 + **모든 옵션**

### 쿼리 패턴 비교

#### ❌ 잘못된 패턴: INNER JOIN + 직접 WHERE 조건

```java
// 잘못된 구현 - 조건 만족 옵션만 반환됨!
private BooleanBuilder buildPredicate(AuctionHistorySearchRequest c, QAuctionHistory ah) {
    BooleanBuilder builder = new BooleanBuilder();

    if (c.itemOptionSearchRequest() != null) {
        QAuctionItemOption aio = QAuctionItemOption.auctionItemOption;
        // ❌ 문제: io에 직접 조건을 걸면 조건 만족 옵션만 반환됨
        builder.and(aio.balance.goe(10));
    }

    return builder;
}
```

**문제점:**
- 조건을 만족하는 옵션만 반환
- 같은 거래내역의 다른 옵션들 필터링됨
- EXPLAIN 결과: `filtered = 19%`

#### ✅ 올바른 패턴: IN 서브쿼리

**SQL 예시:**
```sql
SELECT *
FROM auction_history ah
INNER JOIN auction_item_option io ON ah.auction_buy_id = io.auction_history_id
WHERE ah.item_top_category = '근거리 장비'
  -- 서브쿼리로 조건 만족하는 거래내역 ID만 찾기
  AND ah.auction_buy_id IN (
    SELECT io2.auction_history_id
    FROM auction_item_option io2
    WHERE ((io2.option_type = '공격' AND io2.option_value2 >= 2)
           OR (io2.option_type = '밸런스' AND io2.option_value2 >= 5))
  );
-- 메인 JOIN은 조건 없이 모든 옵션 조회!
```

### AuctionHistoryQueryDslRepository 구현

**올바른 구현:**

```java
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

public Page<AuctionHistory> search(
        AuctionHistorySearchRequest condition, Pageable pageable) {
    QAuctionHistory ah = QAuctionHistory.auctionHistory;
    QAuctionItemOption aio = QAuctionItemOption.auctionItemOption;

    // 1단계: 거래내역 조건 빌드
    BooleanBuilder historyBuilder = buildHistoryPredicate(condition, ah);

    // 2단계: 옵션 조건이 있으면 서브쿼리 추가
    if (condition.itemOptionSearchRequest() != null) {
        // 서브쿼리용 별도 QAuctionItemOption 인스턴스
        QAuctionItemOption subOption = new QAuctionItemOption("subOption");
        BooleanBuilder optionBuilder = buildItemOptionConditions(
            condition.itemOptionSearchRequest(),
            subOption
        );

        // 서브쿼리: 옵션 조건을 만족하는 auction_history_id 찾기
        JPAQuery<String> subQuery = JPAExpressions
            .select(subOption.auctionHistoryId)
            .from(subOption)
            .where(optionBuilder)
            .distinct();

        // 메인 쿼리에 서브쿼리 결과 적용
        historyBuilder.and(ah.auctionBuyId.in(subQuery));
    }

    // 3단계: 모든 옵션과 함께 조회 (LEFT JOIN - 조건 없음!)
    List<AuctionHistory> content = queryFactory
        .selectFrom(ah)
        .leftJoin(ah.auctionItemOptions, aio).fetchJoin()  // 조건 없이 모든 옵션 조회
        .where(historyBuilder)
        .distinct()
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    Long total = queryFactory
        .select(ah.countDistinct())
        .from(ah)
        .where(historyBuilder)  // JOIN 없이 count
        .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0L : total);
}

/**
 * 옵션 검색 조건 빌드 (서브쿼리용)
 * 주의: 메인 JOIN의 WHERE에 직접 사용 금지!
 */
private BooleanBuilder buildItemOptionConditions(
        ItemOptionSearchRequest opt,
        QAuctionItemOption aio) {
    BooleanBuilder builder = new BooleanBuilder();

    // 1. Balance
    if (opt.balanceSearch() != null && opt.balanceSearch().balance() != null) {
        if ("UP".equals(opt.balanceSearch().balanceStandard())) {
            builder.and(aio.balance.goe(opt.balanceSearch().balance()));
        } else if ("DOWN".equals(opt.balanceSearch().balanceStandard())) {
            builder.and(aio.balance.loe(opt.balanceSearch().balance()));
        }
    }

    // ... 나머지 옵션들도 동일 패턴으로 구현

    return builder;
}
```

### 성능 비교

| 항목 | 직접 WHERE | 서브쿼리 (권장) |
|------|-----------|---------------|
| **반환 옵션** | 조건 만족만 | ✅ 모든 옵션 |
| **요구사항 충족** | ❌ | ✅ |
| **MySQL 최적화** | 일반 JOIN | FirstMatch |
| **filtered** | 19% | 100% |
| **중복 제거** | 필요 | DISTINCT 사용 |

---

## 📊 검색 조건 타입 정리

| 검색 옵션명 | 파라미터 | 타입 | Standard | 검색 로직 |
|-----------|---------|------|----------|----------|
| 밸런스 | Balance | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 크리티컬 | Critical | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 방어력 | Defense | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 에르그 | ErgFrom, ErgTo | tinyint | - | Between |
| 에르그 등급 | ErgRank | string | - | Equals ('S등급', 'A등급', 'B등급') |
| 마법 방어력 | MagicDefense | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 마법 보호 | MagicProtect | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 최대 공격력 | MaxAttackFrom, MaxAttackTo | int | - | Between |
| 최대 내구력 | MaximumDurability | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 최대 부상률 | MaxInjuryRateFrom, MaxInjuryRateTo | tinyint | - | Between |
| 숙련도 | Proficiency | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 보호 | Protect | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 남은 거래 횟수 | RemainingTransactionCount | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 남은 전용 해제 가능 횟수 | RemainingUnsealCount | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 남은 사용 횟수 | RemainingUseCount | tinyint | UP/DOWN | UP: >=, DOWN: <= |
| 착용 제한 | WearingRestrictions | string | - | Equals |

**참고:**
- **UP (↑)**: 이상 (Greater Than or Equal, `>=`)
- **DOWN (↓)**: 이하 (Less Than or Equal, `<=`)
- **가격 (Price)**: 이미 `PriceSearchRequest`로 구현되어 있으므로 **제외**

---

## ✅ 체크리스트

### 1단계: DB 및 Migration
- [ ] MySQL 8 DDL 작성 (`auction_search_option_metadata` 테이블)
- [ ] Flyway V 스크립트 작성 (테이블 생성)
- [ ] Flyway R 스크립트 작성 (초기 데이터 INSERT, 1-17번 항목)

### 2단계: 검색 조건 메타데이터 제공 API
- [ ] Entity: `AuctionSearchOptionMetadata` 생성
- [ ] Repository Port: `AuctionSearchOptionRepositoryPort` 인터페이스 작성
- [ ] Repository PortImpl: `AuctionSearchOptionRepositoryPortImpl` 구현
- [ ] JPA Repository: `AuctionSearchOptionJpaRepository` 작성
- [ ] Service: `AuctionSearchOptionService` 구현
- [ ] Response DTO: `SearchOptionMetadataResponse`, `FieldMetadata` 작성
- [ ] Controller: `AuctionSearchOptionController` 구현 (`GET /api/search-option`)

### 3단계: Item Option 검색 기능
- [ ] Request DTO: 개별 옵션 Search Request Record 17개 생성
  - [ ] BalanceSearchRequest
  - [ ] CriticalSearchRequest
  - [ ] DefenseSearchRequest
  - [ ] ErgSearchRequest
  - [ ] ErgRankSearchRequest
  - [ ] MagicDefenseSearchRequest
  - [ ] MagicProtectSearchRequest
  - [ ] MaxAttackSearchRequest
  - [ ] MaximumDurabilitySearchRequest
  - [ ] MaxInjuryRateSearchRequest
  - [ ] ProficiencySearchRequest
  - [ ] ProtectSearchRequest
  - [ ] RemainingTransactionCountSearchRequest
  - [ ] RemainingUnsealCountSearchRequest
  - [ ] RemainingUseCountSearchRequest
  - [ ] WearingRestrictionsSearchRequest
  - [ ] (PriceSearchRequest는 이미 존재)
- [ ] Request DTO: `ItemOptionSearchRequest` 통합 Record 생성
- [ ] `AuctionHistorySearchRequest`에 `ItemOptionSearchRequest` 필드 추가
- [ ] `AuctionHistoryQueryDslRepository.buildPredicate()` 확장
  - [ ] QAuctionItemOption JOIN 추가
  - [ ] 각 옵션별 동적 조건 추가 (17개)
- [ ] `AuctionHistoryQueryDslRepository.search()` JOIN 수정

### 4단계: 테스트
- [ ] `AuctionSearchOptionService` 단위 테스트
- [ ] `AuctionSearchOptionController` 통합 테스트 (REST Docs)
- [ ] `AuctionHistoryQueryDslRepository` 검색 테스트 (ItemOption 조건)
- [ ] `AuctionHistoryService` 검색 테스트 업데이트

### 5단계: 문서화
- [ ] Swagger/OpenAPI 문서 업데이트
- [ ] Spring REST Docs 생성
- [ ] README 업데이트 (새 API 엔드포인트 추가)

---

## 🔗 관련 파일 경로

```
open-api-batch-server/
├── src/main/resources/db/migration/
│   ├── V{next}__create_auction_search_option_metadata.sql  // 새로 생성
│   └── R__insert_auction_search_option_metadata.sql  // 새로 생성
├── src/main/java/until/the/eternity/
│   ├── auctionsearchoption/  // 새로 생성
│   │   ├── application/service/AuctionSearchOptionService.java
│   │   ├── domain/
│   │   │   ├── entity/AuctionSearchOptionMetadata.java
│   │   │   └── repository/AuctionSearchOptionRepositoryPort.java
│   │   ├── infrastructure/persistence/
│   │   │   ├── AuctionSearchOptionJpaRepository.java
│   │   │   └── AuctionSearchOptionRepositoryPortImpl.java
│   │   └── interfaces/rest/
│   │       ├── AuctionSearchOptionController.java
│   │       └── dto/response/SearchOptionMetadataResponse.java
│   └── auctionhistory/
│       ├── infrastructure/persistence/
│       │   └── AuctionHistoryQueryDslRepository.java  // 수정
│       └── interfaces/rest/dto/request/
│           ├── AuctionHistorySearchRequest.java  // 수정
│           └── ItemOptionSearchRequest.java  // 새로 생성
│           └── (17개 개별 Search Request)  // 새로 생성
└── auction_history_search_param.txt  // 참고 문서
```

---

## 🚨 주의사항

1. **display_order는 UNIQUE 제약** 조건 필수
2. **Repository는 Port <-> PortImpl 패턴** 반드시 준수
3. **PriceSearchRequest**는 기존 PricePerUnitSearchRequest에서 이름 변경된 것 (중복 구현 ❌)
4. **미정의 항목 (19-37번)** 제외하고 구현
5. **Standard (UP/DOWN)**은 프론트에서 화살표 아이콘으로 표시
   - UP: ↑ (이상, `>=`)
   - DOWN: ↓ (이하, `<=`)
6. **Flyway V, R 스크립트 모두 작성** 필수
7. **JSON 필드는 required 포함** 버전 사용

---

## 📝 참고 자료

- 원본 파라미터 정의: `auction_history_search_param.txt`
- 프로젝트 구조: `CLAUDE.md`
- Clean Architecture 패턴: 기존 `auctionhistory` 도메인 참고
- Flyway 컨벤션: `src/main/resources/db/migration/` 기존 스크립트 참고

---

**작성자:** Claude Code
**최종 승인:** 사용자 리뷰 완료 (2025-10-20)
