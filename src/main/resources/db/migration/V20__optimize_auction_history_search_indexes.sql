-- Optimize auction history search indexes for fixed filters:
-- item_top_category, item_sub_category, date_auction_buy

-- Replace legacy index that does not align with date-range + date sort pattern
DROP INDEX idx_top_sub_item ON auction_history;

-- Main search index:
-- Equality filters first, range/sort column last
CREATE INDEX idx_ah_top_sub_date
    ON auction_history (item_top_category, item_sub_category, date_auction_buy DESC);

-- Item option subquery index:
-- Supports option_type filtering + grouping by auction_history_id
CREATE INDEX idx_ahio_type_history
    ON auction_history_item_option (option_type, auction_history_id);
