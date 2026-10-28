create table item_option_value_info
(
    option_type      varchar(100) null comment '아이템 옵션 유형',
    option_sub_type  varchar(100) null comment '아이템 옵션 하위 유형',
    option_value     varchar(255) null comment '아이템 옵션 값',
    option_value2    varchar(255) null comment '아이템 옵션 값 2',
    option_desc      text         null comment '아이템 옵션 부가 정보',
    constraint uq_item_option_value_info unique (option_type(10), option_sub_type(10), option_value(10), option_value2(20))
)
    comment '아이템 옵션 정보 테이블';

-- 초기 데이터 적재 쿼리
-- insert into item_option_value_info (option_type, option_sub_type, option_value, option_value2, option_desc) select distinct option_type, option_sub_type, option_value, option_value2, option_desc from auction_item_option;