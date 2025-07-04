-- 일간 최저가 이력
CREATE TABLE item_daily_min_price (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name         VARCHAR(255) NOT NULL
      COMMENT '아이템 이름',
    min_price         BIGINT       NOT NULL
      COMMENT '기록된 최저 단가 (거래내역이 없으면 레코드 자체를 생성하지 않음)',
    date_auction_buy  DATE         NOT NULL
      COMMENT '거래 일자 (해당 데이터가 저장된 일시보다 9시간 전 일자)',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
      COMMENT '해당 데이터가 저장된 일시', -- 데이터 적재 시간 추적용
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
      COMMENT '해당 가격이 발견된 시각 (거래 발생 시각)',

    UNIQUE KEY uq_item_week (item_name, date_auction_buy)
) COMMENT '아이템별 최근 일주일 최저가 테이블';

CREATE INDEX idx_item_daily_min_price_item_name_date_auction_buy
    ON item_daily_min_price (item_name, date_auction_buy);

-- 주간 최저가 이력
CREATE TABLE item_weekly_min_price_history (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name          VARCHAR(255) NOT NULL
       COMMENT '아이템 이름',
    week_start_date    DATE         NOT NULL
       COMMENT '해당 주의 시작일 (월요일)',
    week_end_date      DATE         NOT NULL
       COMMENT '해당 주의 종료일 (일요일, 해당 데이터가 저장된 일자)',
    min_price          BIGINT       NOT NULL
       COMMENT '기록된 최저 단가 (거래내역이 없으면 레코드 자체를 생성하지 않음)',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
       COMMENT '해당 데이터가 저장된 일시',

    UNIQUE KEY uq_item_week (item_name, week_start_date)
) COMMENT '아이템별 주차별 최저가 기록 테이블';

CREATE INDEX idx_item_weekly_min_price_history_item_name_week_start_date
    ON item_weekly_min_price_history (item_name, week_start_date);