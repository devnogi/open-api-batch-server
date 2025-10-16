create table metalware_info
(
    metalware   VARCHAR(50) not null primary key comment '세공',
    level_attribute VARCHAR(50) comment '레벨별 능력치 효과',
    max_level TINYINT comment '레벨 최대 수치',
    limit_break_level TINYINT comment '한계 돌파 레벨 최대 수치'
)
    comment '세공 정보 테이블';

-- 데이터 적재 쿼리
--insert into metalware_info (metalware)
--select distinct regexp_replace(regexp_substr(option_value, ' ?[^\(]+'), ' ?[0-9][0-9]? ?레벨', '') as metalware
--from auction_item_option
--where option_type = '세공 옵션';