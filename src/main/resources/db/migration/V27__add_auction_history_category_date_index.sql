-- 카테고리 + 거래일자 범위 검색/카운트 최적화
-- 기존 idx_ah_top_sub_name_date는 item_name이 중간 컬럼이라
-- item_name 조건이 없는 top/sub/date 질의에서 비효율이 발생할 수 있음
CREATE INDEX idx_ah_top_sub_date
    ON auction_history (item_top_category, item_sub_category, date_auction_buy DESC);
