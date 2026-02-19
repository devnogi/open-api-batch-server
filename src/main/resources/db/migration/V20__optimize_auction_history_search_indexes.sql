-- Optimize auction history search indexes for fixed filters:
-- item_top_category, item_sub_category, date_auction_buy

-- Replace legacy index that does not align with date-range + date sort pattern
DROP INDEX idx_top_sub_item ON auction_history;

-- Main search index:
-- Equality filters first, range/sort column last
-- date_auction_buy는 where 조건에 무조건 포함 및 높은 확률로 정렬 조건
CREATE INDEX idx_ah_top_sub_name_date
    ON auction_history (item_top_category, item_sub_category, item_name, date_auction_buy DESC);

-- Item option subquery index:
-- Supports option_type filtering + grouping by auction_history_id
-- 옵션 검색 서브 쿼리를 위한 인덱스
CREATE INDEX idx_ahio_type_history
    ON auction_history_item_option (option_type, auction_history_id);