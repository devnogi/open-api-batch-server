-- 검색 필터 없이 검색 시 사용되는 기본 검색 속도 보장 인덱스
CREATE INDEX idx_ah_date_auction_buy
    ON auction_history (date_auction_buy);

-- 검색 필터 없이 검색 시 사용되는 기본 검색 속도 보장 인덱스
CREATE INDEX idx_ah_auction_price_per_unit
    ON auction_history (auction_price_per_unit);

-- 검색 필터 없이 검색 시 사용되는 기본 검색 속도 보장 인덱스
CREATE INDEX idx_ar_date_auction_expire
    ON auction_realtime_item (date_auction_expire);

-- 검색 필터 없이 검색 시 사용되는 기본 검색 속도 보장 인덱스
CREATE INDEX idx_ar_auction_price_per_unit
    ON auction_realtime_item (auction_price_per_unit);