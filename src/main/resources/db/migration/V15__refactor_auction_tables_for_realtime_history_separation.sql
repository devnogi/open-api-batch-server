-- V15: Refactor auction tables for realtime/history separation
-- 1. auction_item_option -> auction_history_item_option (rename, remove auction_item_id)
-- 2. auction_item -> auction_realtime_item (rename, add category columns)
-- 3. Create auction_realtime_item_option table

-- ============================================================
-- Step 1: Modify auction_item_option table
-- ============================================================

-- 1-1. Drop foreign key constraint for auction_item_id
ALTER TABLE auction_item_option DROP FOREIGN KEY fk_option_item;

-- 1-2. Drop auction_item_id column
ALTER TABLE auction_item_option DROP COLUMN auction_item_id;

-- 1-3. Rename table to auction_history_item_option
RENAME TABLE auction_item_option TO auction_history_item_option;

-- ============================================================
-- Step 2: Modify auction_item table
-- ============================================================

-- 2-1. Add category columns
ALTER TABLE auction_item ADD COLUMN item_sub_category VARCHAR(25) NOT NULL DEFAULT '' COMMENT '아이템 하위 카테고리';
ALTER TABLE auction_item ADD COLUMN item_top_category VARCHAR(25) NOT NULL DEFAULT '' COMMENT '아이템 상위 카테고리';

-- 2-2. Rename table to auction_realtime_item
RENAME TABLE auction_item TO auction_realtime_item;

-- 2-3. Create indexes for auction_realtime_item
CREATE INDEX idx_realtime_top_sub_item
    ON auction_realtime_item (item_top_category, item_sub_category, item_name);

CREATE INDEX idx_realtime_sub_category_expire
    ON auction_realtime_item (item_sub_category, date_auction_expire DESC);

-- ============================================================
-- Step 3: Create auction_realtime_item_option table
-- ============================================================

CREATE TABLE auction_realtime_item_option (
    id VARCHAR(36) NOT NULL COMMENT 'ItemOption의 고유 식별자 (UUID)',
    auction_realtime_item_id BIGINT NOT NULL COMMENT 'auction_realtime_item 테이블의 외래 키',
    option_type VARCHAR(100) COMMENT '아이템 옵션 유형',
    option_sub_type VARCHAR(100) COMMENT '아이템 옵션 하위 유형',
    option_value VARCHAR(255) COMMENT '아이템 옵션 값',
    option_value2 VARCHAR(255) COMMENT '아이템 옵션 값 2',
    option_desc TEXT COMMENT '아이템 옵션 부가 정보',
    PRIMARY KEY (id),
    CONSTRAINT fk_realtime_option_item FOREIGN KEY (auction_realtime_item_id)
        REFERENCES auction_realtime_item(id) ON DELETE CASCADE
) COMMENT='실시간 경매장 아이템 옵션 정보 테이블';
