#
ALTER TABLE auction_history ADD item_sub_category VARCHAR(25) NOT NULL COMMENT '아이템 하위 카테고리';
ALTER TABLE auction_history ADD item_top_category VARCHAR(25) NOT NULL COMMENT '아이템 상위 카테고리';

#
ALTER TABLE auction_history ADD date_register DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '해당 경매장 거래 내역을 수집한 시각';