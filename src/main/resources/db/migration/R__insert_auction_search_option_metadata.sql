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
