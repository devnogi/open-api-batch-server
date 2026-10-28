-- BATCH 조회 API P99 tail latency 개선 인덱스
-- - horn_bugle 최신 목록은 date_send DESC + id DESC 순서로 안정적인 top-N 조회를 수행한다.
-- - all-time highest ranking은 auction_history 전체 정렬 대신 가격/거래 ID 인덱스로 top-N 조회한다.

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'horn_bugle_world_history'
      AND index_name = 'idx_horn_bugle_date_send_id'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_horn_bugle_date_send_id ON horn_bugle_world_history (date_send DESC, id DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'horn_bugle_world_history'
      AND index_name = 'idx_horn_bugle_server_date_send_id'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_horn_bugle_server_date_send_id ON horn_bugle_world_history (server_name, date_send DESC, id DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'auction_history'
      AND index_name = 'idx_auction_history_price_buy_id'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_auction_history_price_buy_id ON auction_history (auction_price_per_unit DESC, auction_buy_id DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
