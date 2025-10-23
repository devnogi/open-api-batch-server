# Auction Item Option 데이터 구조 분석

## DB 테이블: `auction_item_option`

### 테이블 구조
- `option_type`: VARCHAR(100) - 옵션 종류 (예: "밸런스", "크리티컬", "공격")
- `option_sub_type`: VARCHAR(100) - 옵션 서브 타입
- `option_value`: VARCHAR(255) - 옵션 값 (텍스트)
- `option_value2`: VARCHAR(255) - 옵션 값2 (숫자 또는 텍스트)

### 통계 (실제 데이터 분석)

| option_type | 레코드 수 | distinct_value2 | 설명 |
|-------------|-----------|-----------------|------|
| 공격 | 1,349 | 124 | option_value2에 숫자 저장 |
| 밸런스 | 1,326 | 0 | option_value에 저장 가능 |
| 크리티컬 | 1,257 | 0 | option_value에 저장 가능 |
| 내구력 | 1,959 | 43 | option_value2에 숫자 저장 |
| 부상률 | 225 | 14 | option_value2에 숫자 저장 |
| 방어력 | 522 | 0 | option_value에 저장 가능 |
| 보호 | 154 | 0 | option_value에 저장 가능 |
| 남은 거래 횟수 | 2,788 | 0 | option_value에 저장 가능 |

## 요구사항 매핑: Request DTO → DB option_type

| Request DTO 필드 | DB option_type | 값 저장 위치 | 검색 방식 |
|------------------|----------------|-------------|----------|
| Balance | "밸런스" | option_value OR option_value2 | CAST 후 비교 |
| Critical | "크리티컬" | option_value OR option_value2 | CAST 후 비교 |
| Defense | "방어력" | option_value OR option_value2 | CAST 후 비교 |
| Erg | ? | ? | 미확인 |
| ErgRank | "에코스톤 등급"? | option_value | 문자열 비교 |
| MagicDefense | "마법 방어력" | option_value OR option_value2 | CAST 후 비교 |
| MagicProtect | "마법 보호" | option_value OR option_value2 | CAST 후 비교 |
| MaxAttack | "공격" | option_value2 | CAST 후 비교 |
| MaximumDurability | "내구력" | option_value2 | CAST 후 비교 |
| MaxInjuryRate | "부상률" | option_value2 | CAST 후 비교 |
| Proficiency | "숙련" | option_value OR option_value2 | CAST 후 비교 |
| Protect | "보호" | option_value OR option_value2 | CAST 후 비교 |
| RemainingTransactionCount | "남은 거래 횟수" | option_value | 숫자 파싱 후 비교 |
| RemainingUnsealCount | "남은 전용 해제 가능 횟수" | option_value | 숫자 파싱 후 비교 |
| RemainingUseCount | "남은 사용 횟수" | option_value | 숫자 파싱 후 비교 |
| WearingRestrictions | ? | option_value | 문자열 비교 |

## QueryDSL 검색 로직 전략

### 1. 숫자 비교 (UP/DOWN 기준)
```java
// option_value2가 우선, 없으면 option_value 파싱
BooleanExpression condition = aio.optionType.eq("밸런스")
    .and(
        aio.optionValue2.isNotNull()
            .and(aio.optionValue2.castToNum(Integer.class).goe(value))
        .or(
            aio.optionValue.contains(value.toString())
        )
    );
```

### 2. 범위 검색 (From/To)
```java
BooleanExpression condition = aio.optionType.eq("공격")
    .and(aio.optionValue2.castToNum(Integer.class).between(from, to));
```

### 3. 문자열 검색
```java
BooleanExpression condition = aio.optionValue.eq("S등급");
```

## 주의사항
- option_value와 option_value2 둘 다 확인 필요
- 숫자 변환 시 CAST 또는 문자열 파싱 필요
- NULL 체크 필수
