CREATE TABLE item_info (
    name VARCHAR(255) NOT NULL COMMENT '아이템의 이름',
    sub_category VARCHAR(25) NOT NULL COMMENT '아이템 하위 카테고리',
    top_category VARCHAR(25) NOT NULL COMMENT '아이템 상위 카테고리',
    description VARCHAR(255) COMMENT '아이템의 설명',
    inventory_width TINYINT COMMENT '인벤토리 가로 크기',
    inventory_height TINYINT COMMENT '인벤토리 세로 크기',
    inventory_max_bundle_count INT COMMENT '최대 번들 가능 개수',
    history VARCHAR(3000) COMMENT '아이템의 역사',
    acquisition_method VARCHAR(3000) COMMENT '입수 방법',
    store_sales_price VARCHAR(3000) COMMENT '1개 상점 판매가',
    weapon_type VARCHAR(25) COMMENT '공격 속도 및 무기 타입',
    repair VARCHAR(25) COMMENT '수리',
    max_alteration_count TINYINT COMMENT '최대 개조 횟수'

) COMMENT='아이템 정보 테이블';

-- 초기 데이터 적재 쿼리
-- INSERT INTO item_info (name, top_category, sub_category) select distinct item_name, item_sub_category, item_sub_category from auction_history;