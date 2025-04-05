CREATE TABLE horn_bugle_world_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    character_name VARCHAR(100) NOT NULL COMMENT '발화한 유저의 캐릭터명',
    message TEXT NOT NULL COMMENT '발화한 거대한 외침의 뿔피리 내용',
    date_send DATETIME NOT NULL COMMENT '발화한 시각 (UTC0)'
) COMMENT='거대한 외침의 뿔피리 내역 테이블';

CREATE TABLE auction_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_name VARCHAR(255) NOT NULL COMMENT '해당 아이템의 이름',
    item_display_name VARCHAR(255) NOT NULL COMMENT '아이템 전체 이름',
    item_count BIGINT NOT NULL COMMENT '해당 아이템의 판매 수량',
    auction_price_per_unit BIGINT NOT NULL COMMENT '해당 아이템의 개당 판매 가격',
    date_auction_expire DATETIME NOT NULL COMMENT '만료 기한 (해당 아이템의 판매 종료 일시)',
    date_register DATETIME NOT NULL COMMENT '해당 아이템 정보를 수집한 시각'
) COMMENT='현재 경매장에서 판매 중인 아이템 정보 테이블';

CREATE TABLE auction_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    item_name VARCHAR(255) NOT NULL COMMENT '해당 아이템의 이름',
    item_display_name VARCHAR(255) NOT NULL COMMENT '아이템 전체 이름',
    item_count BIGINT NOT NULL COMMENT '해당 아이템의 거래 수량',
    auction_price_per_unit BIGINT NOT NULL COMMENT '해당 아이템의 개당 거래 가격',
    date_auction_buy DATETIME NOT NULL COMMENT '해당 아이템이 거래된 시각 (UTC0)',
    auction_buy_id VARCHAR(255) NOT NULL UNIQUE COMMENT '해당 아이템 거래의 고유 식별 ID'
) COMMENT='아이템 거래 이력 테이블';

CREATE TABLE auction_item_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고유 식별자',
    auction_item_id BIGINT NULL COMMENT 'auction_item 테이블의 외래 키 (판매중인 아이템)',
    auction_history_id BIGINT NULL COMMENT 'auction_history 테이블의 외래 키 (과거 거래 아이템)',
    option_type VARCHAR(100) COMMENT '아이템 옵션 유형',
    option_sub_type VARCHAR(100) COMMENT '아이템 옵션 하위 유형',
    option_value VARCHAR(255) COMMENT '아이템 옵션 값',
    option_value2 VARCHAR(255) COMMENT '아이템 옵션 값 2',
    option_desc TEXT COMMENT '아이템 옵션 부가 정보',
    CONSTRAINT fk_option_item FOREIGN KEY (auction_item_id) REFERENCES auction_item(id) ON DELETE CASCADE,
    CONSTRAINT fk_option_history FOREIGN KEY (auction_history_id) REFERENCES auction_history(id) ON DELETE CASCADE
) COMMENT='아이템의 옵션 정보를 저장하는 테이블 (판매중 혹은 거래된 아이템에 연결됨)';
