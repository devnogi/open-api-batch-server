# Auction Item Option 검색 기능 구현 계획서

> 작성일: 2025-10-20
> 참조 문서: AUCTION_SEARCH_OPTION_REQUIREMENTS.md
> 프로젝트: open-api-batch-server

---

## 📑 목차

1. [Phase 1: DB 스키마 및 Migration](#phase-1-db-스키마-및-migration)
2. [Phase 2: 검색 조건 메타데이터 API 구현](#phase-2-검색-조건-메타데이터-api-구현)
3. [Phase 3: Item Option 검색 Request DTO 구현](#phase-3-item-option-검색-request-dto-구현)
4. [Phase 4: QueryDSL 검색 로직 확장](#phase-4-querydsl-검색-로직-확장)
5. [Phase 5: 테스트 코드 작성](#phase-5-테스트-코드-작성)
6. [Phase 6: 문서화 및 마무리](#phase-6-문서화-및-마무리)

---

## Phase 1: DB 스키마 및 Migration

### 목표
- `auction_search_option_metadata` 테이블 생성
- 초기 데이터 INSERT (17개 검색 옵션)

### 작업 순서

#### 1.1. Flyway 버전 확인

**명령어:**
```bash
ls src/main/resources/db/migration/
```

**목적:** 다음 마이그레이션 버전 번호 확인

#### 1.2. V 스크립트 작성 (테이블 생성)

**파일명:** `V{next_version}__create_auction_search_option_metadata.sql`
**예시:** `V7__create_auction_search_option_metadata.sql` (기존 V6까지 있다고 가정)

**파일 경로:**
```
src/main/resources/db/migration/V7__create_auction_search_option_metadata.sql
```

**파일 내용:**
```sql
-- 경매 검색 옵션 메타데이터 테이블 생성
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

#### 1.3. R 스크립트 작성 (초기 데이터)

**파일명:** `R__insert_auction_search_option_metadata.sql`

**파일 경로:**
```
src/main/resources/db/migration/R__insert_auction_search_option_metadata.sql
```

**파일 내용:**
```sql
-- 경매 검색 옵션 메타데이터 초기 데이터
-- Repeatable: 데이터 변경 시 자동 재실행

-- 기존 데이터 삭제 (Repeatable 스크립트이므로)
DELETE FROM `auction_search_option_metadata`;

-- 1. 밸런스
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '밸런스',
    JSON_OBJECT(
        'Balance', JSON_OBJECT('type', 'tinyint', 'required', false),
        'BalanceStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    1,
    true
);

-- 2. 크리티컬
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '크리티컬',
    JSON_OBJECT(
        'Critical', JSON_OBJECT('type', 'tinyint', 'required', false),
        'CriticalStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    2,
    true
);

-- 3. 방어력
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '방어력',
    JSON_OBJECT(
        'Defense', JSON_OBJECT('type', 'tinyint', 'required', false),
        'DefenseStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    3,
    true
);

-- 4. 에르그 (범위)
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '에르그',
    JSON_OBJECT(
        'ErgFrom', JSON_OBJECT('type', 'tinyint', 'required', false),
        'ErgTo', JSON_OBJECT('type', 'tinyint', 'required', false)
    ),
    4,
    true
);

-- 5. 에르그 등급
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '에르그 등급',
    JSON_OBJECT(
        'ErgRank', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('S등급', 'A등급', 'B등급'), 'required', false)
    ),
    5,
    true
);

-- 6. 마법 방어력
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '마법 방어력',
    JSON_OBJECT(
        'MagicDefense', JSON_OBJECT('type', 'tinyint', 'required', false),
        'MagicDefenseStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    6,
    true
);

-- 7. 마법 보호
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '마법 보호',
    JSON_OBJECT(
        'MagicProtect', JSON_OBJECT('type', 'tinyint', 'required', false),
        'MagicProtectStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    7,
    true
);

-- 8. 최대 공격력
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '최대 공격력',
    JSON_OBJECT(
        'MaxAttackFrom', JSON_OBJECT('type', 'int', 'required', false),
        'MaxAttackTo', JSON_OBJECT('type', 'int', 'required', false)
    ),
    8,
    true
);

-- 9. 최대 내구력
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '최대 내구력',
    JSON_OBJECT(
        'MaximumDurability', JSON_OBJECT('type', 'tinyint', 'required', false),
        'MaximumDurabilityStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    9,
    true
);

-- 10. 최대 부상률
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '최대 부상률',
    JSON_OBJECT(
        'MaxInjuryRateFrom', JSON_OBJECT('type', 'tinyint', 'required', false),
        'MaxInjuryRateTo', JSON_OBJECT('type', 'tinyint', 'required', false)
    ),
    10,
    true
);

-- 11. 숙련도
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '숙련도',
    JSON_OBJECT(
        'Proficiency', JSON_OBJECT('type', 'tinyint', 'required', false),
        'ProficiencyStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    11,
    true
);

-- 12. 보호
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '보호',
    JSON_OBJECT(
        'Protect', JSON_OBJECT('type', 'tinyint', 'required', false),
        'ProtectStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    12,
    true
);

-- 13. 남은 거래 횟수
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '남은 거래 횟수',
    JSON_OBJECT(
        'RemainingTransactionCount', JSON_OBJECT('type', 'tinyint', 'required', false),
        'RemainingTransactionCountStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    13,
    true
);

-- 14. 남은 전용 해제 가능 횟수
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '남은 전용 해제 가능 횟수',
    JSON_OBJECT(
        'RemainingUnsealCount', JSON_OBJECT('type', 'tinyint', 'required', false),
        'RemainingUnsealCountStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    14,
    true
);

-- 15. 남은 사용 횟수
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '남은 사용 횟수',
    JSON_OBJECT(
        'RemainingUseCount', JSON_OBJECT('type', 'tinyint', 'required', false),
        'RemainingUseCountStandard', JSON_OBJECT('type', 'string', 'allowedValues', JSON_ARRAY('UP', 'DOWN'), 'required', false)
    ),
    15,
    true
);

-- 16. 착용 제한
INSERT INTO `auction_search_option_metadata`
(`search_option_name`, `search_condition_json`, `display_order`, `is_active`)
VALUES (
    '착용 제한',
    JSON_OBJECT(
        'WearingRestrictions', JSON_OBJECT('type', 'string', 'required', false)
    ),
    16,
    true
);
```

#### 1.4. Migration 실행 및 검증

**명령어:**
```bash
./gradlew flywayMigrate
```

**검증 쿼리:**
```sql
-- 테이블 생성 확인
SHOW CREATE TABLE auction_search_option_metadata;

-- 데이터 확인
SELECT id, search_option_name, display_order, is_active
FROM auction_search_option_metadata
ORDER BY display_order;

-- JSON 데이터 확인
SELECT search_option_name, JSON_PRETTY(search_condition_json)
FROM auction_search_option_metadata
WHERE id = 1;
```

---

## Phase 2: 검색 조건 메타데이터 API 구현

### 목표
- Clean Architecture 패턴으로 검색 조건 메타데이터 조회 API 구현
- `GET /api/search-option` 엔드포인트 제공

### 작업 순서

#### 2.1. 디렉토리 구조 생성

**생성할 디렉토리:**
```
src/main/java/until/the/eternity/auctionsearchoption/
├── application/
│   └── service/
├── domain/
│   ├── entity/
│   └── repository/
├── infrastructure/
│   └── persistence/
└── interfaces/
    └── rest/
        └── dto/
            └── response/
```

#### 2.2. Entity 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/domain/entity/AuctionSearchOptionMetadata.java`

```java
package until.the.eternity.auctionsearchoption.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auction_search_option_metadata")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionSearchOptionMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_option_name", nullable = false, length = 100)
    private String searchOptionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_condition_json", nullable = false, columnDefinition = "JSON")
    private String searchConditionJson;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### 2.3. Repository Port 인터페이스 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/domain/repository/AuctionSearchOptionRepositoryPort.java`

```java
package until.the.eternity.auctionsearchoption.domain.repository;

import java.util.List;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;

public interface AuctionSearchOptionRepositoryPort {

    /**
     * 모든 활성화된 검색 옵션 조회 (정렬 순서대로)
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    List<AuctionSearchOptionMetadata> findAllActive();

    /**
     * 모든 검색 옵션 조회 (정렬 순서대로)
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    List<AuctionSearchOptionMetadata> findAll();
}
```

#### 2.4. JPA Repository 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/infrastructure/persistence/AuctionSearchOptionJpaRepository.java`

```java
package until.the.eternity.auctionsearchoption.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;

@Repository
interface AuctionSearchOptionJpaRepository
        extends JpaRepository<AuctionSearchOptionMetadata, Long> {

    List<AuctionSearchOptionMetadata> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<AuctionSearchOptionMetadata> findAllByOrderByDisplayOrderAsc();
}
```

#### 2.5. Repository Port 구현체 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/infrastructure/persistence/AuctionSearchOptionRepositoryPortImpl.java`

```java
package until.the.eternity.auctionsearchoption.infrastructure.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;

@Component
@RequiredArgsConstructor
class AuctionSearchOptionRepositoryPortImpl implements AuctionSearchOptionRepositoryPort {

    private final AuctionSearchOptionJpaRepository jpaRepository;

    @Override
    public List<AuctionSearchOptionMetadata> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    public List<AuctionSearchOptionMetadata> findAll() {
        return jpaRepository.findAllByOrderByDisplayOrderAsc();
    }
}
```

#### 2.6. Response DTO 작성

**파일 1:** `src/main/java/until/the/eternity/auctionsearchoption/interfaces/rest/dto/response/FieldMetadata.java`

```java
package until.the.eternity.auctionsearchoption.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검색 조건 필드 메타데이터")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldMetadata(
        @Schema(description = "필드 타입", example = "tinyint") String type,
        @Schema(description = "필수 여부", example = "false") Boolean required,
        @Schema(description = "허용된 값 목록 (Enum인 경우)", example = "[\"UP\", \"DOWN\"]")
                List<String> allowedValues) {}
```

**파일 2:** `src/main/java/until/the/eternity/auctionsearchoption/interfaces/rest/dto/response/SearchOptionMetadataResponse.java`

```java
package until.the.eternity.auctionsearchoption.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "검색 옵션 메타데이터 응답")
public record SearchOptionMetadataResponse(
        @Schema(description = "검색 옵션 ID", example = "1") Long id,
        @Schema(description = "검색 옵션명", example = "밸런스") String searchOptionName,
        @Schema(description = "검색 조건 상세")
                Map<String, FieldMetadata> searchCondition,
        @Schema(description = "정렬 순서", example = "1") Integer displayOrder) {}
```

#### 2.7. Service 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/application/service/AuctionSearchOptionService.java`

```java
package until.the.eternity.auctionsearchoption.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.FieldMetadata;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSearchOptionService {

    private final AuctionSearchOptionRepositoryPort repositoryPort;
    private final ObjectMapper objectMapper;

    /**
     * 모든 활성화된 검색 옵션 조회
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<SearchOptionMetadataResponse> getAllActiveSearchOptions() {
        List<AuctionSearchOptionMetadata> entities = repositoryPort.findAllActive();

        return entities.stream().map(this::toResponse).toList();
    }

    private SearchOptionMetadataResponse toResponse(AuctionSearchOptionMetadata entity) {
        Map<String, FieldMetadata> searchCondition = parseJsonToFieldMetadata(entity.getSearchConditionJson());

        return new SearchOptionMetadataResponse(
                entity.getId(),
                entity.getSearchOptionName(),
                searchCondition,
                entity.getDisplayOrder());
    }

    private Map<String, FieldMetadata> parseJsonToFieldMetadata(String json) {
        try {
            TypeReference<Map<String, FieldMetadata>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Failed to parse JSON to FieldMetadata: {}", json, e);
            throw new IllegalStateException("JSON 파싱 실패", e);
        }
    }
}
```

#### 2.8. Controller 작성

**파일:** `src/main/java/until/the/eternity/auctionsearchoption/interfaces/rest/AuctionSearchOptionController.java`

```java
package until.the.eternity.auctionsearchoption.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.auctionsearchoption.application.service.AuctionSearchOptionService;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;
import until.the.eternity.common.response.ApiResponse;

@Tag(name = "Auction Search Option", description = "경매 검색 옵션 API")
@RestController
@RequestMapping("/api/search-option")
@RequiredArgsConstructor
public class AuctionSearchOptionController {

    private final AuctionSearchOptionService service;

    @Operation(summary = "검색 옵션 메타데이터 조회", description = "경매 검색에 사용 가능한 모든 옵션 메타데이터를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchOptionMetadataResponse>>> getSearchOptions() {
        List<SearchOptionMetadataResponse> searchOptions = service.getAllActiveSearchOptions();

        return ResponseEntity.ok(
                ApiResponse.success(searchOptions, "검색 옵션 조회 성공"));
    }
}
```

#### 2.9. API 테스트

**수동 테스트:**
```bash
curl -X GET http://localhost:8080/api/search-option
```

**예상 응답:**
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
          "required": false,
          "allowedValues": ["UP", "DOWN"]
        }
      },
      "displayOrder": 1
    }
  ],
  "timestamp": "2025-10-20T12:00:00Z"
}
```

---

## Phase 3: Item Option 검색 Request DTO 구현

### 목표
- 16개 개별 Search Request Record 생성
- ItemOptionSearchRequest 통합 Record 생성
- AuctionHistorySearchRequest에 필드 추가

### 작업 순서

#### 3.1. 개별 Search Request Record 생성 (16개)

**디렉토리:**
```
src/main/java/until/the/eternity/auctionhistory/interfaces/rest/dto/request/
```

**파일 목록 및 내용:**

**1) BalanceSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "밸런스 검색 조건")
public record BalanceSearchRequest(
        @Schema(description = "밸런스 값", example = "10") Integer balance,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String balanceStandard) {}
```

**2) CriticalSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크리티컬 검색 조건")
public record CriticalSearchRequest(
        @Schema(description = "크리티컬 값", example = "30") Integer critical,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String criticalStandard) {}
```

**3) DefenseSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방어력 검색 조건")
public record DefenseSearchRequest(
        @Schema(description = "방어력 값", example = "5") Integer defense,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String defenseStandard) {}
```

**4) ErgSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에르그 검색 조건 (범위)")
public record ErgSearchRequest(
        @Schema(description = "에르그 최소값", example = "10") Integer ergFrom,
        @Schema(description = "에르그 최대값", example = "50") Integer ergTo) {}
```

**5) ErgRankSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에르그 등급 검색 조건")
public record ErgRankSearchRequest(
        @Schema(description = "에르그 등급", example = "S등급", allowableValues = {"S등급", "A등급", "B등급"})
                String ergRank) {}
```

**6) MagicDefenseSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마법 방어력 검색 조건")
public record MagicDefenseSearchRequest(
        @Schema(description = "마법 방어력 값", example = "3") Integer magicDefense,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String magicDefenseStandard) {}
```

**7) MagicProtectSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마법 보호 검색 조건")
public record MagicProtectSearchRequest(
        @Schema(description = "마법 보호 값", example = "2") Integer magicProtect,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String magicProtectStandard) {}
```

**8) MaxAttackSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 공격력 검색 조건 (범위)")
public record MaxAttackSearchRequest(
        @Schema(description = "최대 공격력 최소값", example = "50") Integer maxAttackFrom,
        @Schema(description = "최대 공격력 최대값", example = "100") Integer maxAttackTo) {}
```

**9) MaximumDurabilitySearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 내구력 검색 조건")
public record MaximumDurabilitySearchRequest(
        @Schema(description = "최대 내구력 값", example = "20") Integer maximumDurability,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String maximumDurabilityStandard) {}
```

**10) MaxInjuryRateSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 부상률 검색 조건 (범위)")
public record MaxInjuryRateSearchRequest(
        @Schema(description = "최대 부상률 최소값", example = "10") Integer maxInjuryRateFrom,
        @Schema(description = "최대 부상률 최대값", example = "30") Integer maxInjuryRateTo) {}
```

**11) ProficiencySearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "숙련도 검색 조건")
public record ProficiencySearchRequest(
        @Schema(description = "숙련도 값", example = "15") Integer proficiency,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String proficiencyStandard) {}
```

**12) ProtectSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보호 검색 조건")
public record ProtectSearchRequest(
        @Schema(description = "보호 값", example = "1") Integer protect,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String protectStandard) {}
```

**13) RemainingTransactionCountSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "남은 거래 횟수 검색 조건")
public record RemainingTransactionCountSearchRequest(
        @Schema(description = "남은 거래 횟수", example = "5") Integer remainingTransactionCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String remainingTransactionCountStandard) {}
```

**14) RemainingUnsealCountSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "남은 전용 해제 가능 횟수 검색 조건")
public record RemainingUnsealCountSearchRequest(
        @Schema(description = "남은 전용 해제 가능 횟수", example = "3") Integer remainingUnsealCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String remainingUnsealCountStandard) {}
```

**15) RemainingUseCountSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "남은 사용 횟수 검색 조건")
public record RemainingUseCountSearchRequest(
        @Schema(description = "남은 사용 횟수", example = "10") Integer remainingUseCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String remainingUseCountStandard) {}
```

**16) WearingRestrictionsSearchRequest.java**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "착용 제한 검색 조건")
public record WearingRestrictionsSearchRequest(
        @Schema(description = "착용 제한", example = "자이언트 전용") String wearingRestrictions) {}
```

#### 3.2. ItemOptionSearchRequest 통합 Record 생성

**파일:** `src/main/java/until/the/eternity/auctionhistory/interfaces/rest/dto/request/ItemOptionSearchRequest.java`

```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이템 옵션 검색 조건 통합")
public record ItemOptionSearchRequest(
        @Schema(description = "밸런스 검색 조건") BalanceSearchRequest balanceSearch,
        @Schema(description = "크리티컬 검색 조건") CriticalSearchRequest criticalSearch,
        @Schema(description = "방어력 검색 조건") DefenseSearchRequest defenseSearch,
        @Schema(description = "에르그 검색 조건") ErgSearchRequest ergSearch,
        @Schema(description = "에르그 등급 검색 조건") ErgRankSearchRequest ergRankSearch,
        @Schema(description = "마법 방어력 검색 조건")
                MagicDefenseSearchRequest magicDefenseSearch,
        @Schema(description = "마법 보호 검색 조건") MagicProtectSearchRequest magicProtectSearch,
        @Schema(description = "최대 공격력 검색 조건") MaxAttackSearchRequest maxAttackSearch,
        @Schema(description = "최대 내구력 검색 조건")
                MaximumDurabilitySearchRequest maximumDurabilitySearch,
        @Schema(description = "최대 부상률 검색 조건")
                MaxInjuryRateSearchRequest maxInjuryRateSearch,
        @Schema(description = "숙련도 검색 조건") ProficiencySearchRequest proficiencySearch,
        @Schema(description = "보호 검색 조건") ProtectSearchRequest protectSearch,
        @Schema(description = "남은 거래 횟수 검색 조건")
                RemainingTransactionCountSearchRequest remainingTransactionCountSearch,
        @Schema(description = "남은 전용 해제 가능 횟수 검색 조건")
                RemainingUnsealCountSearchRequest remainingUnsealCountSearch,
        @Schema(description = "남은 사용 횟수 검색 조건")
                RemainingUseCountSearchRequest remainingUseCountSearch,
        @Schema(description = "착용 제한 검색 조건")
                WearingRestrictionsSearchRequest wearingRestrictionsSearch) {}
```

#### 3.3. AuctionHistorySearchRequest 수정

**파일:** `src/main/java/until/the/eternity/auctionhistory/interfaces/rest/dto/request/AuctionHistorySearchRequest.java`

**수정 내용:**
```java
package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** 경매 히스토리 검색 조건 DTO - 페이지네이션 포함 */
@Schema(description = "경매 거래내역 검색 조건")
public record AuctionHistorySearchRequest(
        @Schema(description = "아이템 이름 (like 검색)", example = "페러시우스 타이탄 블레이드")
                String itemName,
        @Schema(description = "대분류 카테고리", example = "근거리 장비") String itemTopCategory,
        @Schema(description = "소분류 카테고리", example = "검") String itemSubCategory,
        @Schema(description = "거래 가격", example = "10000000") String auction_price_per_unit,
        @Schema(description = "거래 일자", example = "2025-10-20") String date_auction_buy,
        @Schema(description = "가격 검색 조건") PriceSearchRequest priceSearchRequest,
        @Schema(description = "아이템 옵션 검색 조건")
                ItemOptionSearchRequest itemOptionSearchRequest) {}
```

---

## Phase 4: QueryDSL 검색 로직 확장

### 목표
- AuctionHistoryQueryDslRepository에 ItemOption 검색 조건 추가
- 서브쿼리 패턴을 사용한 올바른 검색 로직 구현
- 조건을 만족하는 거래내역의 **모든 옵션** 반환

### 🔍 쿼리 패턴 분석 및 선택

#### 요구사항 명확화

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
  - 옵션 4: 내구력 30 (조건 불만족이지만 반환)

**반환:** 거래내역 + 옵션 1, 2, 3, 4 **모두**

#### 쿼리 패턴 비교

**❌ 패턴 1: INNER JOIN + 직접 WHERE 조건 (부적합)**
```sql
SELECT *
FROM auction_history ah
INNER JOIN auction_item_option io ON ah.auction_buy_id = io.auction_history_id
WHERE ah.item_top_category = '근거리 장비'
  AND ((io.option_type = '공격' AND io.option_value2 >= 2)
       OR (io.option_type = '밸런스' AND io.option_value2 >= 5))
```

**문제점:**
- `io`에 직접 WHERE 조건을 걸기 때문에 **조건을 만족하는 옵션만** 반환
- 같은 거래내역의 다른 옵션들은 필터링되어 제외됨
- EXPLAIN 결과: `io.filtered = 19%` (조건 만족 옵션만)

**✅ 패턴 2: IN 서브쿼리 (권장)**
```sql
SELECT *
FROM auction_history ah
INNER JOIN auction_item_option io ON ah.auction_buy_id = io.auction_history_id
WHERE ah.item_top_category = '근거리 장비'
  AND ah.auction_buy_id IN (
    SELECT io2.auction_history_id
    FROM auction_item_option io2
    WHERE ((io2.option_type = '공격' AND io2.option_value2 >= 2)
           OR (io2.option_type = '밸런스' AND io2.option_value2 >= 5))
  )
```

**장점:**
- 서브쿼리로 조건 만족하는 거래내역 ID만 찾기
- 메인 쿼리에서 해당 거래내역의 **모든 옵션** 조회 (조건 없음!)
- EXPLAIN 결과: `io.filtered = 100%` (모든 옵션 반환)
- MySQL 옵티마이저가 FirstMatch로 최적화

#### 실제 데이터 검증 결과

동일한 거래내역 ID를 조회했을 때:

| 쿼리 패턴 | option_count | 반환 옵션 |
|---------|-------------|---------|
| 패턴 1 (직접 WHERE) | 1 | 공격:20 만 |
| 패턴 2 (서브쿼리) | 7 | 공격:20, 아이템 색상, 내구력:11, 밸런스, 크리티컬 등 **모두** |

**결론: 패턴 2 (IN 서브쿼리) 사용 필수**

### 작업 순서

#### 4.1. AuctionHistoryQueryDslRepository 수정 (서브쿼리 패턴)

**파일:** `src/main/java/until/the/eternity/auctionhistory/infrastructure/persistence/AuctionHistoryQueryDslRepository.java`

**수정 내용:**

```java
package until.the.eternity.auctionhistory.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.entity.QAuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.*;
import until.the.eternity.auctionitemoption.domain.entity.QAuctionItemOption;

@Component
@RequiredArgsConstructor
class AuctionHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 경매 거래내역 검색 (옵션 조건 포함)
     *
     * 검색 흐름:
     * 1. 옵션 조건을 만족하는 거래내역 ID를 서브쿼리로 찾기
     * 2. 거래내역 조건으로 필터링
     * 3. 해당 거래내역의 모든 옵션을 함께 조회 (LEFT JOIN)
     */
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
        List<AuctionHistory> content =
                queryFactory
                        .selectFrom(ah)
                        .leftJoin(ah.auctionItemOptions, aio)
                        .fetchJoin()
                        .where(historyBuilder)
                        .distinct()  // 중복 제거
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        // Count 쿼리 (JOIN 없이 실행)
        Long total =
                queryFactory
                        .select(ah.countDistinct())
                        .from(ah)
                        .where(historyBuilder)
                        .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    /**
     * 거래내역 기본 조건 빌드 (카테고리, 아이템명, 가격, 거래일자)
     */
    private BooleanBuilder buildHistoryPredicate(
            AuctionHistorySearchRequest c,
            QAuctionHistory ah) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건들
        if (c.itemTopCategory() != null && !c.itemTopCategory().isBlank()) {
            builder.and(ah.itemTopCategory.eq(c.itemTopCategory()));
        }
        if (c.itemSubCategory() != null && !c.itemSubCategory().isBlank()) {
            builder.and(ah.itemSubCategory.eq(c.itemSubCategory()));
        }
        if (c.itemName() != null && !c.itemName().isBlank()) {
            builder.and(ah.itemName.containsIgnoreCase(c.itemName()));
        }

        // 가격 조건 (PriceSearchRequest가 있으면)
        if (c.priceSearchRequest() != null) {
            PriceSearchRequest price = c.priceSearchRequest();
            if (price.priceFrom() != null) {
                builder.and(ah.auctionPricePerUnit.goe(price.priceFrom()));
            }
            if (price.priceTo() != null) {
                builder.and(ah.auctionPricePerUnit.loe(price.priceTo()));
            }
        }

        // 거래 일자 조건
        if (c.date_auction_buy() != null && !c.date_auction_buy().isBlank()) {
            // 날짜 파싱 및 조건 추가 로직
            // TODO: 날짜 범위 검색 구현
        }

        return builder;
    }

    /**
     * 옵션 검색 조건 빌드 (서브쿼리용)
     *
     * 주의: 이 메서드는 서브쿼리에서만 사용됩니다.
     * 반환된 BooleanBuilder는 메인 JOIN의 WHERE에 직접 사용하면 안 됩니다!
     */
    private BooleanBuilder buildItemOptionConditions(
            ItemOptionSearchRequest opt,
            QAuctionItemOption aio) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. Balance
        if (opt.balanceSearch() != null && opt.balanceSearch().balance() != null) {
            addStandardCondition(
                    builder,
                    aio.balance,
                    opt.balanceSearch().balance(),
                    opt.balanceSearch().balanceStandard());
        }

        // 2. Critical
        if (opt.criticalSearch() != null && opt.criticalSearch().critical() != null) {
            addStandardCondition(
                    builder,
                    aio.critical,
                    opt.criticalSearch().critical(),
                    opt.criticalSearch().criticalStandard());
        }

        // 3. Defense
        if (opt.defenseSearch() != null && opt.defenseSearch().defense() != null) {
            addStandardCondition(
                    builder,
                    aio.defense,
                    opt.defenseSearch().defense(),
                    opt.defenseSearch().defenseStandard());
        }

        // 4. Erg (범위)
        if (opt.ergSearch() != null) {
            if (opt.ergSearch().ergFrom() != null) {
                builder.and(aio.erg.goe(opt.ergSearch().ergFrom()));
            }
            if (opt.ergSearch().ergTo() != null) {
                builder.and(aio.erg.loe(opt.ergSearch().ergTo()));
            }
        }

        // 5. ErgRank
        if (opt.ergRankSearch() != null && opt.ergRankSearch().ergRank() != null) {
            builder.and(aio.ergRank.eq(opt.ergRankSearch().ergRank()));
        }

        // 6. MagicDefense
        if (opt.magicDefenseSearch() != null
                && opt.magicDefenseSearch().magicDefense() != null) {
            addStandardCondition(
                    builder,
                    aio.magicDefense,
                    opt.magicDefenseSearch().magicDefense(),
                    opt.magicDefenseSearch().magicDefenseStandard());
        }

        // 7. MagicProtect
        if (opt.magicProtectSearch() != null
                && opt.magicProtectSearch().magicProtect() != null) {
            addStandardCondition(
                    builder,
                    aio.magicProtect,
                    opt.magicProtectSearch().magicProtect(),
                    opt.magicProtectSearch().magicProtectStandard());
        }

        // 8. MaxAttack (범위)
        if (opt.maxAttackSearch() != null) {
            if (opt.maxAttackSearch().maxAttackFrom() != null) {
                builder.and(aio.maxAttack.goe(opt.maxAttackSearch().maxAttackFrom()));
            }
            if (opt.maxAttackSearch().maxAttackTo() != null) {
                builder.and(aio.maxAttack.loe(opt.maxAttackSearch().maxAttackTo()));
            }
        }

        // 9. MaximumDurability
        if (opt.maximumDurabilitySearch() != null
                && opt.maximumDurabilitySearch().maximumDurability() != null) {
            addStandardCondition(
                    builder,
                    aio.maximumDurability,
                    opt.maximumDurabilitySearch().maximumDurability(),
                    opt.maximumDurabilitySearch().maximumDurabilityStandard());
        }

        // 10. MaxInjuryRate (범위)
        if (opt.maxInjuryRateSearch() != null) {
            if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null) {
                builder.and(
                        aio.maxInjuryRate.goe(
                                opt.maxInjuryRateSearch().maxInjuryRateFrom()));
            }
            if (opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                builder.and(
                        aio.maxInjuryRate.loe(opt.maxInjuryRateSearch().maxInjuryRateTo()));
            }
        }

        // 11. Proficiency
        if (opt.proficiencySearch() != null
                && opt.proficiencySearch().proficiency() != null) {
            addStandardCondition(
                    builder,
                    aio.proficiency,
                    opt.proficiencySearch().proficiency(),
                    opt.proficiencySearch().proficiencyStandard());
        }

        // 12. Protect
        if (opt.protectSearch() != null && opt.protectSearch().protect() != null) {
            addStandardCondition(
                    builder,
                    aio.protect,
                    opt.protectSearch().protect(),
                    opt.protectSearch().protectStandard());
        }

        // 13. RemainingTransactionCount
        if (opt.remainingTransactionCountSearch() != null
                && opt.remainingTransactionCountSearch().remainingTransactionCount()
                        != null) {
            addStandardCondition(
                    builder,
                    aio.remainingTransactionCount,
                    opt.remainingTransactionCountSearch().remainingTransactionCount(),
                    opt.remainingTransactionCountSearch()
                            .remainingTransactionCountStandard());
        }

        // 14. RemainingUnsealCount
        if (opt.remainingUnsealCountSearch() != null
                && opt.remainingUnsealCountSearch().remainingUnsealCount() != null) {
            addStandardCondition(
                    builder,
                    aio.remainingUnsealCount,
                    opt.remainingUnsealCountSearch().remainingUnsealCount(),
                    opt.remainingUnsealCountSearch().remainingUnsealCountStandard());
        }

        // 15. RemainingUseCount
        if (opt.remainingUseCountSearch() != null
                && opt.remainingUseCountSearch().remainingUseCount() != null) {
            addStandardCondition(
                    builder,
                    aio.remainingUseCount,
                    opt.remainingUseCountSearch().remainingUseCount(),
                    opt.remainingUseCountSearch().remainingUseCountStandard());
        }

        // 16. WearingRestrictions
        if (opt.wearingRestrictionsSearch() != null
                && opt.wearingRestrictionsSearch().wearingRestrictions() != null) {
            builder.and(
                    aio.wearingRestrictions.eq(
                            opt.wearingRestrictionsSearch().wearingRestrictions()));
        }

        return builder;
    }

    /**
     * UP/DOWN 기준 조건 추가 헬퍼 메서드
     */
    private void addStandardCondition(
            BooleanBuilder builder,
            com.querydsl.core.types.dsl.NumberPath<Integer> field,
            Integer value,
            String standard) {
        if ("UP".equals(standard)) {
            builder.and(field.goe(value)); // 이상 (>=)
        } else if ("DOWN".equals(standard)) {
            builder.and(field.loe(value)); // 이하 (<=)
        } else {
            builder.and(field.eq(value)); // 같음
        }
    }
}
```

---

## Phase 5: 테스트 코드 작성

### 목표
- 단위 테스트 및 통합 테스트 작성
- Spring REST Docs 문서 생성

### 작업 순서

#### 5.1. AuctionSearchOptionService 단위 테스트

**파일:** `src/test/java/until/the/eternity/auctionsearchoption/application/service/AuctionSearchOptionServiceTest.java`

```java
package until.the.eternity.auctionsearchoption.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;

@ExtendWith(MockitoExtension.class)
class AuctionSearchOptionServiceTest {

    @Mock private AuctionSearchOptionRepositoryPort repositoryPort;
    @InjectMocks private AuctionSearchOptionService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("활성화된 검색 옵션을 조회한다")
    void getAllActiveSearchOptions_should_return_active_options() {
        // given
        AuctionSearchOptionMetadata entity = createMockEntity();
        when(repositoryPort.findAllActive()).thenReturn(List.of(entity));

        // when
        List<SearchOptionMetadataResponse> result = service.getAllActiveSearchOptions();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).searchOptionName()).isEqualTo("밸런스");
        verify(repositoryPort).findAllActive();
    }

    private AuctionSearchOptionMetadata createMockEntity() {
        // Mock 객체 생성 로직
        return mock(AuctionSearchOptionMetadata.class);
    }
}
```

#### 5.2. AuctionSearchOptionController 통합 테스트 (REST Docs)

**파일:** `src/test/java/until/the/eternity/auctionsearchoption/interfaces/rest/AuctionSearchOptionControllerTest.java`

```java
package until.the.eternity.auctionsearchoption.interfaces.rest;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import until.the.eternity.auctionsearchoption.application.service.AuctionSearchOptionService;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.FieldMetadata;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;

@WebMvcTest(AuctionSearchOptionController.class)
@AutoConfigureRestDocs
class AuctionSearchOptionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AuctionSearchOptionService service;

    @Test
    @DisplayName("GET /api/search-option - 검색 옵션 메타데이터 조회")
    void getSearchOptions_should_return_search_options() throws Exception {
        // given
        SearchOptionMetadataResponse response =
                new SearchOptionMetadataResponse(
                        1L,
                        "밸런스",
                        Map.of(
                                "Balance",
                                new FieldMetadata("tinyint", false, null),
                                "BalanceStandard",
                                new FieldMetadata(
                                        "string", false, List.of("UP", "DOWN"))),
                        1);

        when(service.getAllActiveSearchOptions()).thenReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/search-option"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].searchOptionName").value("밸런스"))
                .andDo(document("search-option-get"));
    }
}
```

#### 5.3. AuctionHistoryQueryDslRepository 테스트

**파일:** `src/test/java/until/the/eternity/auctionhistory/infrastructure/persistence/AuctionHistoryQueryDslRepositoryTest.java`

```java
// ItemOption 검색 조건 테스트 추가
@Test
@DisplayName("ItemOption - Balance 조건으로 검색한다 (UP)")
void search_with_balance_up_condition() {
    // given
    BalanceSearchRequest balanceSearch = new BalanceSearchRequest(10, "UP");
    ItemOptionSearchRequest itemOptionSearch =
            new ItemOptionSearchRequest(
                    balanceSearch,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
    AuctionHistorySearchRequest request =
            new AuctionHistorySearchRequest(
                    null, null, null, null, null, null, itemOptionSearch);
    Pageable pageable = PageRequest.of(0, 10);

    // when
    Page<AuctionHistory> result = repository.search(request, pageable);

    // then
    assertThat(result.getContent())
            .allMatch(
                    ah ->
                            ah.getAuctionItemOptions().stream()
                                    .anyMatch(aio -> aio.getBalance() >= 10));
}
```

---

## Phase 6: 문서화 및 마무리

### 목표
- Swagger 문서 확인
- Spring REST Docs 생성
- README 업데이트

### 작업 순서

#### 6.1. Swagger UI 확인

**접속:**
```
http://localhost:8080/swagger-ui/index.html
```

**확인 사항:**
- `/api/search-option` 엔드포인트 존재
- Request/Response 스키마 정상 표시
- 16개 Search Request DTO 스키마 정상

#### 6.2. Spring REST Docs 생성

**명령어:**
```bash
./gradlew asciidoctor
```

**생성 위치:**
```
build/docs/asciidoc/index.html
```

#### 6.3. README 업데이트

**추가할 내용:**
```markdown
## 새로운 API 엔드포인트

### GET /api/search-option
경매 검색 옵션 메타데이터를 조회합니다.

**Response:**
- 16개 검색 옵션 메타데이터 반환
- 각 옵션의 파라미터 타입 및 허용 값 포함

### POST /api/auction-history/search
경매 거래내역을 검색합니다.

**새로운 검색 조건:**
- `itemOptionSearchRequest`: 아이템 옵션 기반 검색
  - Balance, Critical, Defense 등 16개 옵션 지원
  - UP/DOWN 기준 검색 (이상/이하)
  - 범위 검색 (From/To)
```

---

## 📊 구현 진행 체크리스트

### Phase 1: DB 및 Migration ✅
- [ ] Flyway 버전 확인
- [ ] V 스크립트 작성 및 실행
- [ ] R 스크립트 작성 및 실행
- [ ] DB 데이터 검증

### Phase 2: 메타데이터 API ✅
- [ ] Entity 작성
- [ ] Repository Port/Impl 작성
- [ ] JPA Repository 작성
- [ ] Response DTO 작성
- [ ] Service 작성
- [ ] Controller 작성
- [ ] API 수동 테스트

### Phase 3: Request DTO ✅
- [ ] 16개 개별 Search Request 작성
- [ ] ItemOptionSearchRequest 통합 작성
- [ ] AuctionHistorySearchRequest 수정
- [ ] Spotless 포맷팅 적용

### Phase 4: QueryDSL ✅
- [ ] buildPredicate 확장
- [ ] addItemOptionConditions 구현
- [ ] addStandardCondition 헬퍼 메서드 작성
- [ ] JOIN 수정
- [ ] 빌드 및 컴파일 확인

### Phase 5: 테스트 ✅
- [ ] AuctionSearchOptionService 단위 테스트
- [ ] AuctionSearchOptionController REST Docs 테스트
- [ ] AuctionHistoryQueryDslRepository 검색 테스트
- [ ] 전체 테스트 실행 및 통과 확인

### Phase 6: 문서화 ✅
- [ ] Swagger UI 확인
- [ ] Spring REST Docs 생성
- [ ] README 업데이트
- [ ] CHANGELOG 작성

---

## 🚀 실행 순서 요약

1. **Migration 실행**
   ```bash
   ./gradlew flywayMigrate
   ```

2. **Phase별 순차 구현**
   - Phase 1 → 2 → 3 → 4 → 5 → 6

3. **각 Phase 완료 후 빌드 및 테스트**
   ```bash
   ./gradlew clean build
   ```

4. **Spotless 포맷팅 자동 적용**
   ```bash
   ./gradlew spotlessApply
   ```

5. **최종 검증**
   ```bash
   ./gradlew test
   ./gradlew bootRun
   # Swagger UI 접속하여 API 확인
   ```

---

## 📊 개발 현황 (Implementation Status)

> **최종 업데이트:** 2025-10-20 23:59
> **진행 상황:** Phase 2 완료 (33% 완료)

### ✅ 완료된 작업

#### Phase 1: DB 스키마 및 Migration ✅ (100% 완료)
**완료 일시:** 2025-10-20 23:47

- ✅ Flyway V11 스크립트 작성 (`V11__create_auction_search_option_metadata.sql`)
- ✅ Flyway R 스크립트 작성 (`R__insert_auction_search_option_metadata.sql`)
- ✅ Migration 실행 성공 (version v11)
- ✅ DB 데이터 검증 완료 (16개 검색 옵션 INSERT 확인)

**생성된 파일:**
```
src/main/resources/db/migration/
├── V11__create_auction_search_option_metadata.sql
└── R__insert_auction_search_option_metadata.sql
```

**DB 검증 결과:**
- 테이블: `auction_search_option_metadata` 생성 완료
- 데이터: 16개 레코드 (밸런스, 크리티컬, 방어력 등)
- JSON 구조: type, required, allowedValues 포함

---

#### Phase 2: 검색 조건 메타데이터 API 구현 ✅ (100% 완료)
**완료 일시:** 2025-10-20 23:59

**구현된 레이어:**

1. **Domain Layer**
   - ✅ Entity: `AuctionSearchOptionMetadata.java`
   - ✅ Repository Port: `AuctionSearchOptionRepositoryPort.java`

2. **Infrastructure Layer**
   - ✅ JPA Repository: `AuctionSearchOptionJpaRepository.java`
   - ✅ Repository PortImpl: `AuctionSearchOptionRepositoryPortImpl.java`

3. **Application Layer**
   - ✅ Service: `AuctionSearchOptionService.java`
     - JSON 파싱 로직 (ObjectMapper)
     - Entity → DTO 변환

4. **Interface Layer**
   - ✅ Response DTO: `FieldMetadata.java`, `SearchOptionMetadataResponse.java`
   - ✅ Controller: `AuctionSearchOptionController.java`

**API 엔드포인트:**
```
GET /api/search-option
```

**API 테스트 결과:**
- ✅ HTTP 200 OK 응답
- ✅ 16개 검색 옵션 정상 반환
- ✅ JSON 구조 정확 (type, required, allowedValues)
- ✅ displayOrder 순서대로 정렬

**생성된 파일:**
```
src/main/java/until/the/eternity/auctionsearchoption/
├── application/service/AuctionSearchOptionService.java
├── domain/
│   ├── entity/AuctionSearchOptionMetadata.java
│   └── repository/AuctionSearchOptionRepositoryPort.java
├── infrastructure/persistence/
│   ├── AuctionSearchOptionJpaRepository.java
│   └── AuctionSearchOptionRepositoryPortImpl.java
└── interfaces/rest/
    ├── AuctionSearchOptionController.java
    └── dto/response/
        ├── FieldMetadata.java
        └── SearchOptionMetadataResponse.java
```

---

### 🚧 진행 중인 작업

**현재 단계:** 없음 (다음 Phase 대기)

---

### 📋 남은 작업

#### Phase 3: Item Option 검색 Request DTO 구현 (예정)
**예상 작업량:** 17개 파일 생성

- [ ] 16개 개별 Search Request Record 생성
  - [ ] BalanceSearchRequest.java
  - [ ] CriticalSearchRequest.java
  - [ ] DefenseSearchRequest.java
  - [ ] ErgSearchRequest.java
  - [ ] ErgRankSearchRequest.java
  - [ ] MagicDefenseSearchRequest.java
  - [ ] MagicProtectSearchRequest.java
  - [ ] MaxAttackSearchRequest.java
  - [ ] MaximumDurabilitySearchRequest.java
  - [ ] MaxInjuryRateSearchRequest.java
  - [ ] ProficiencySearchRequest.java
  - [ ] ProtectSearchRequest.java
  - [ ] RemainingTransactionCountSearchRequest.java
  - [ ] RemainingUnsealCountSearchRequest.java
  - [ ] RemainingUseCountSearchRequest.java
  - [ ] WearingRestrictionsSearchRequest.java
- [ ] ItemOptionSearchRequest.java (통합 Record)
- [ ] AuctionHistorySearchRequest.java 수정

---

#### Phase 4: QueryDSL 검색 로직 확장 (예정)
- [ ] AuctionHistoryQueryDslRepository.buildPredicate() 확장
- [ ] addItemOptionConditions() 메서드 구현
- [ ] addStandardCondition() 헬퍼 메서드 작성
- [ ] QAuctionItemOption JOIN 추가

---

#### Phase 5: 테스트 코드 작성 (예정)
- [ ] AuctionSearchOptionService 단위 테스트
- [ ] AuctionSearchOptionController REST Docs 테스트
- [ ] AuctionHistoryQueryDslRepository 검색 테스트

---

#### Phase 6: 문서화 및 마무리 (예정)
- [ ] Swagger UI 확인
- [ ] Spring REST Docs 생성
- [ ] README 업데이트

---

### 📝 다음 세션 작업 가이드

**다음 작업:** Phase 3 - Item Option 검색 Request DTO 구현

**시작 방법:**
```bash
# 1. 프로젝트 루트로 이동
cd C:/Users/Desktop/devnogi/open-api-batch-server

# 2. 구현 계획서 확인
cat AUCTION_SEARCH_OPTION_IMPLEMENT_PLAN.md | grep -A 50 "Phase 3"

# 3. 16개 Search Request Record 생성 시작
# 경로: src/main/java/until/the/eternity/auctionhistory/interfaces/rest/dto/request/
```

**참고 문서:**
- 요구사항: `AUCTION_SEARCH_OPTION_REQUIREMENTS.md`
- 구현 계획: `AUCTION_SEARCH_OPTION_IMPLEMENT_PLAN.md` (현재 문서)
- 원본 파라미터: `auction_history_search_param.txt`

---

**작성자:** Claude Code
**최종 검토:** 2025-10-20
