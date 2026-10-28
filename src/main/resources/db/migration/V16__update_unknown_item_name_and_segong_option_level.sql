-- V16: item_name이 '(Unknown)'인 경우 item_display_name으로 업데이트,
--      세공 옵션 파싱 (option_value, option_value2, option_desc)

-- 1. auction_history 테이블: item_name이 '(Unknown)'인 경우 item_display_name으로 업데이트
UPDATE auction_history
SET item_name = item_display_name
WHERE item_name = '(Unknown)';

-- 2. auction_realtime_item 테이블: item_name이 '(Unknown)'인 경우 item_display_name으로 업데이트
UPDATE auction_realtime_item
SET item_name = item_display_name
WHERE item_name = '(Unknown)';

-- 3. auction_history_item_option 테이블: 세공 옵션 파싱
-- 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식 (예: "천옷만들기 품질 보너스 3 레벨", "지력 2레벨")
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '[0-9]+ ?레벨$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?= ?레벨$)'),
    option_value = REGEXP_REPLACE(option_value, ' [0-9]+ ?레벨$', '')
WHERE option_type = '세공 옵션'
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 형식 (예: "매그넘 샷 대미지(20레벨:200 % 증가)")
UPDATE auction_history_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';

-- 4. auction_realtime_item_option 테이블: 세공 옵션 파싱
-- 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식 (예: "천옷만들기 품질 보너스 3 레벨", "지력 2레벨")
UPDATE auction_realtime_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '[0-9]+ ?레벨$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?= ?레벨$)'),
    option_value = REGEXP_REPLACE(option_value, ' [0-9]+ ?레벨$', '')
WHERE option_type = '세공 옵션'
  AND option_value REGEXP '^.+ [0-9]+ ?레벨$';

-- 패턴 2: "스킬명(숫자레벨:효과)" 형식 (예: "매그넘 샷 대미지(20레벨:200 % 증가)")
UPDATE auction_realtime_item_option
SET option_desc = REGEXP_SUBSTR(option_value, '\\([0-9]+레벨:.+\\)$'),
    option_value2 = REGEXP_SUBSTR(option_value, '[0-9]+(?=레벨:)'),
    option_value = REGEXP_REPLACE(option_value, '\\([0-9]+레벨:.+\\)$', '')
WHERE option_type = '세공 옵션'
  AND option_value REGEXP '^.+\\([0-9]+레벨:.+\\)$';
