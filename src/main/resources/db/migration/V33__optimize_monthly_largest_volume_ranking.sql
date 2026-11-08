-- 이번 달 최대 거래액 랭킹의 월 조건과 총 거래액 내림차순 top-N 조회를 지원한다.
-- RankingRepository.findMonthLargestVolume의 표현식과 순서를 동일하게 유지해야 한다.

SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'auction_history'
      AND index_name = 'idx_ah_month_total_price_buy_id'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_ah_month_total_price_buy_id ON auction_history ((YEAR(date_auction_buy)), (MONTH(date_auction_buy)), (auction_price_per_unit * item_count) DESC, auction_buy_id DESC)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
