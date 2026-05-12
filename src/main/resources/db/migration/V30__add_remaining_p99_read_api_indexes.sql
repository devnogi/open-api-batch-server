-- 잔여 p99 조회 API 최적화 인덱스
-- - 전체 거래량 랭킹은 당일 통계를 거래 수량 기준으로 정렬한다.
-- - 주간 거래량 랭킹은 연도/주차 통계를 거래 수량 기준으로 정렬한다.
-- - 일간 아이템 통계는 itemName + category + date range 조건을 사용한다.
-- - 검색 옵션 메타데이터는 active filtering + display order 정렬을 사용한다.

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_daily_statistics'
      AND index_name = 'idx_item_daily_date_total_quantity'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_daily_date_total_quantity ON item_daily_statistics (date_auction_buy, total_quantity DESC)',
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
      AND index_name = 'idx_item_weekly_year_week_total_quantity'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_weekly_year_week_total_quantity ON item_weekly_statistics (year, week_number, total_quantity DESC)',
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
      AND index_name = 'idx_item_daily_item_category_date'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_daily_item_category_date ON item_daily_statistics (item_name(120), item_top_category(80), item_sub_category(80), date_auction_buy)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'auction_search_option_metadata'
      AND index_name = 'idx_search_option_active_display_order'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_search_option_active_display_order ON auction_search_option_metadata (is_active, display_order)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
