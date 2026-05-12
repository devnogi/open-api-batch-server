-- p99 조회 API 최적화 인덱스
-- - weekly top category 통계 조회는 week_start_date 범위 조건을 사용한다.
-- - enchant fullname 필터 조회는 affix_position 조건과 id 정렬을 함께 사용한다.

CREATE INDEX idx_top_category_weekly_statistics_category_start_date
    ON top_category_weekly_statistics (item_top_category, week_start_date);

CREATE INDEX idx_enchant_info_affix_position_id
    ON enchant_info (affix_position, id);
