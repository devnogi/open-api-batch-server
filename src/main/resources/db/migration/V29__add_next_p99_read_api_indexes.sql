-- 추가 p99 조회 API 최적화 인덱스
-- - 카테고리 랭킹은 당일/카테고리 조건으로 좁힌 뒤 가격/수량으로 정렬한다.
-- - 가격 변동 랭킹은 오늘/어제 item_daily_statistics self join을 수행한다.
-- - 주간 아이템 통계는 week_start_date 범위 조건을 사용한다.

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_daily_statistics'
      AND index_name = 'idx_item_daily_date_top_sub_max_price'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_daily_date_top_sub_max_price ON item_daily_statistics (date_auction_buy, item_top_category, item_sub_category, max_price DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_daily_statistics'
      AND index_name = 'idx_item_daily_date_top_sub_quantity'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_daily_date_top_sub_quantity ON item_daily_statistics (date_auction_buy, item_top_category, item_sub_category, total_quantity DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_daily_statistics'
      AND index_name = 'idx_item_daily_date_item_category_change'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_daily_date_item_category_change ON item_daily_statistics (date_auction_buy, item_name(120), item_top_category(80), item_sub_category(80), avg_price, total_quantity)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_weekly_statistics'
      AND index_name = 'idx_item_weekly_item_category_start_date'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_weekly_item_category_start_date ON item_weekly_statistics (item_name(120), item_top_category(80), item_sub_category(80), week_start_date)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
