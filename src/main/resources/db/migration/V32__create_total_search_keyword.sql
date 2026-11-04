CREATE TABLE total_search_keyword (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '통합 검색 키워드 ID',
    keyword VARCHAR(255) NOT NULL COMMENT '사용자에게 표시할 검색어',
    keyword_type VARCHAR(16) NOT NULL COMMENT '검색어 유형: ITEM, MENU',
    target_key VARCHAR(255) NOT NULL COMMENT '프런트엔드가 해석할 안정적인 목적지 키',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_total_search_keyword_type_target (keyword_type, target_key),
    KEY idx_total_search_keyword_keyword (keyword)
) COMMENT='통합 검색 자동완성 키워드';
