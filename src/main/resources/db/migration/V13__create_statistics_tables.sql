-- =====================================================
-- V13: 통계 테이블 생성 및 기존 min_price 테이블 삭제
-- =====================================================
-- 1. 기존 item_daily_min_price 테이블 삭제
-- 2. 새로운 통계 테이블 6개 생성
--    - Daily: Item, Subcategory, TopCategory
--    - Weekly: Item, Subcategory, TopCategory
-- =====================================================

-- 기존 item_daily_min_price 테이블 삭제
DROP TABLE IF EXISTS item_daily_min_price;
DROP TABLE IF EXISTS item_weekly_min_price_history;

-- =====================================================
-- Daily Statistics Tables
-- =====================================================

-- 아이템별 일간 통계
-- TopCategory, SubCategory가 다른 ItemName이 존재함으로 유의
CREATE TABLE item_daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_name VARCHAR(255) NOT NULL COMMENT '아이템 이름',
    item_top_category VARCHAR(255) NOT NULL COMMENT '탑 카테고리',
    item_sub_category VARCHAR(255) NOT NULL COMMENT '서브 카테고리',
    date_auction_buy DATE NOT NULL COMMENT '거래 일자',
    min_price BIGINT NOT NULL COMMENT '최저 단가',
    max_price BIGINT NOT NULL COMMENT '최고 단가',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_item_daily_statistics_item_name_date (item_name, date_auction_buy),
    INDEX idx_item_daily_statistics_item_name_date (item_name, date_auction_buy)
) COMMENT='아이템별 일간 통계';

-- 서브카테고리별 일간 통계
CREATE TABLE subcategory_daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_sub_category VARCHAR(255) NOT NULL COMMENT '아이템 서브 카테고리',
    date_auction_buy DATE NOT NULL COMMENT '거래 일자',
    min_price BIGINT NOT NULL COMMENT '최저 단가',
    max_price BIGINT NOT NULL COMMENT '최고 단가',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_subcategory_daily_statistics_category_date (item_sub_category, date_auction_buy),
    INDEX idx_subcategory_daily_statistics_category_date (item_sub_category, date_auction_buy)
) COMMENT='서브카테고리별 일간 통계';

-- 탑카테고리별 일간 통계
CREATE TABLE top_category_daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_top_category VARCHAR(255) NOT NULL COMMENT '아이템 탑 카테고리',
    date_auction_buy DATE NOT NULL COMMENT '거래 일자',
    min_price BIGINT NOT NULL COMMENT '최저 단가',
    max_price BIGINT NOT NULL COMMENT '최고 단가',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_top_category_daily_statistics_category_date (item_top_category, date_auction_buy),
    INDEX idx_top_category_daily_statistics_category_date (item_top_category, date_auction_buy)
) COMMENT='탑카테고리별 일간 통계';

-- =====================================================
-- Weekly Statistics Tables
-- =====================================================

-- 아이템별 주간 통계
CREATE TABLE item_weekly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_name VARCHAR(255) NOT NULL COMMENT '아이템 이름',
    item_top_category VARCHAR(255) NOT NULL COMMENT '상위 카테고리',
    item_sub_category VARCHAR(255) NOT NULL COMMENT '하위 카테고리',
    year INT NOT NULL COMMENT '연도',
    week_number INT NOT NULL COMMENT '주차 번호',
    week_start_date DATE NOT NULL COMMENT '주 시작일 (월요일)',
    min_price BIGINT NOT NULL COMMENT '최저 단가 (해당 주의 모든 거래 중 최저)',
    max_price BIGINT NOT NULL COMMENT '최고 단가 (해당 주의 모든 거래 중 최고)',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가 (Daily 평균가의 평균)',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_item_weekly_statistics_item_name_year_week (item_name, year, week_number),
    INDEX idx_item_weekly_statistics_item_name_year_week (item_name, year, week_number)
) COMMENT='아이템별 주간 통계';

-- 서브카테고리별 주간 통계
CREATE TABLE subcategory_weekly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_sub_category VARCHAR(255) NOT NULL COMMENT '아이템 서브 카테고리',
    year INT NOT NULL COMMENT '연도',
    week_number INT NOT NULL COMMENT '주차 번호',
    week_start_date DATE NOT NULL COMMENT '주 시작일 (월요일)',
    min_price BIGINT NOT NULL COMMENT '최저 단가 (해당 주의 모든 거래 중 최저)',
    max_price BIGINT NOT NULL COMMENT '최고 단가 (해당 주의 모든 거래 중 최고)',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가 (Daily 평균가의 평균)',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_subcategory_weekly_statistics_category_year_week (item_sub_category, year, week_number),
    INDEX idx_subcategory_weekly_statistics_category_year_week (item_sub_category, year, week_number)
) COMMENT='서브카테고리별 주간 통계';

-- 탑카테고리별 주간 통계
CREATE TABLE top_category_weekly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_top_category VARCHAR(255) NOT NULL COMMENT '아이템 탑 카테고리',
    year INT NOT NULL COMMENT '연도',
    week_number INT NOT NULL COMMENT '주차 번호',
    week_start_date DATE NOT NULL COMMENT '주 시작일 (월요일)',
    min_price BIGINT NOT NULL COMMENT '최저 단가 (해당 주의 모든 거래 중 최저)',
    max_price BIGINT NOT NULL COMMENT '최고 단가 (해당 주의 모든 거래 중 최고)',
    avg_price DECIMAL(15, 2) NOT NULL COMMENT '평균 단가 (Daily 평균가의 평균)',
    total_volume BIGINT NOT NULL COMMENT '거래 총량 (총 거래 금액)',
    total_quantity BIGINT NOT NULL COMMENT '거래 수량 (itemCount 합계)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    UNIQUE KEY uk_top_category_weekly_statistics_category_year_week (item_top_category, year, week_number),
    INDEX idx_top_category_weekly_statistics_category_year_week (item_top_category, year, week_number)
) COMMENT='탑카테고리별 주간 통계';
