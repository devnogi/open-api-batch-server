-- V18: 세공 옵션 미파싱 데이터 마이그레이션
-- V16 이후 신규 유입된 데이터 중 option_value2/option_desc가 NULL인 세공 옵션을 파싱

-- ============================================================
-- 1. auction_history_item_option 테이블
-- ============================================================

-- 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '[0-9]+ ?레벨$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?= ?레벨$)'),
    option_value = REGEXP_REPLACE(option_value, ' [0-9]+ ?레벨$', '')
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 형식
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';

-- 패턴 3: "스킬명 설명텍스트" 형식 (레벨 정보 없이 텍스트 설명만 존재)
UPDATE auction_history_item_option
SET option_desc = SUBSTRING(option_value, LOCATE(' ', option_value) + 1),
    option_value = SUBSTRING_INDEX(option_value, ' ', 1)
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value NOT REGEXP '[0-9]+ ?레벨'
  AND option_value NOT REGEXP '\\([0-9]+레벨:'
  AND LOCATE(' ', option_value) > 0;

-- ============================================================
-- 2. auction_realtime_item_option 테이블
-- ============================================================

-- 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식
UPDATE auction_realtime_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '[0-9]+ ?레벨$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?= ?레벨$)'),
    option_value = REGEXP_REPLACE(option_value, ' [0-9]+ ?레벨$', '')
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 형식
UPDATE auction_realtime_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';

-- 패턴 3: "스킬명 설명텍스트" 형식 (레벨 정보 없이 텍스트 설명만 존재)
UPDATE auction_realtime_item_option
SET option_desc = SUBSTRING(option_value, LOCATE(' ', option_value) + 1),
    option_value = SUBSTRING_INDEX(option_value, ' ', 1)
WHERE option_type = '세공 옵션'
  AND option_value2 IS NULL
  AND option_desc IS NULL
  AND option_value NOT REGEXP '[0-9]+ ?레벨'
  AND option_value NOT REGEXP '\\([0-9]+레벨:'
  AND LOCATE(' ', option_value) > 0;
