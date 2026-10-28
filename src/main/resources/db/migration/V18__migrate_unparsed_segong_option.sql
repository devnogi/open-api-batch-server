-- V18: 세공 옵션 미파싱 데이터 마이그레이션
-- V16 이후 신규 유입된 데이터 중 option_value2/option_desc가 NULL 또는 빈 문자열인 세공 옵션을 파싱
--
-- 지원 패턴:
--   패턴 1: "스킬명 숫자 레벨" (예: "지력 2레벨", "행운 6 레벨")
--   패턴 2: "스킬명(숫자레벨:효과)" (예: "최대생명력(15레벨:37.50 증가)", "매그넘 샷 대미지(20레벨:200 % 증가)")
--           이중 괄호도 처리: "스킬명(설명)(숫자레벨:효과)" (예: "교역 중 이동 속도(교역 강화 의상)(19레벨:57 % 증가)")
--   패턴 3: "스킬명 설명텍스트" (예: "돌진 인간 및 엘프일 때 방패 없이 사용 가능")

-- ============================================================
-- 1. auction_history_item_option 테이블
-- ============================================================

-- 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '[0-9]+ ?레벨$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?= ?레벨$)'),
    option_value = REGEXP_REPLACE(option_value, ' [0-9]+ ?레벨$', '')
WHERE option_type = '세공 옵션'
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 또는 "스킬명(설명)(숫자레벨:효과)" 형식
-- 이중 괄호 케이스(교역 중 이동 속도(교역 강화 의상)(19레벨:57 % 증가))도
-- REGEXP의 greedy/backtrack 특성으로 마지막 "(숫자레벨:" 패턴을 찾아 정상 처리됨
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';

-- 패턴 3: "스킬명 설명텍스트" 형식 (레벨 정보 없이 텍스트 설명만 존재)
-- 첫 번째 공백 기준으로 스킬명과 설명을 분리
UPDATE auction_history_item_option
SET option_desc = SUBSTRING(option_value, LOCATE(' ', option_value) + 1),
    option_value = SUBSTRING_INDEX(option_value, ' ', 1)
WHERE option_type = '세공 옵션'
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
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
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 또는 "스킬명(설명)(숫자레벨:효과)" 형식
UPDATE auction_realtime_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';

-- 패턴 3: "스킬명 설명텍스트" 형식 (레벨 정보 없이 텍스트 설명만 존재)
UPDATE auction_realtime_item_option
SET option_desc = SUBSTRING(option_value, LOCATE(' ', option_value) + 1),
    option_value = SUBSTRING_INDEX(option_value, ' ', 1)
WHERE option_type = '세공 옵션'
  AND (option_value2 IS NULL OR option_value2 = '')
  AND (option_desc IS NULL OR option_desc = '')
  AND option_value NOT REGEXP '[0-9]+ ?레벨'
  AND option_value NOT REGEXP '\\([0-9]+레벨:'
  AND LOCATE(' ', option_value) > 0;
