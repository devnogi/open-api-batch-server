-- auction_item_option의 FK 제약 조건(fk_option_history)을 삭제
ALTER TABLE auction_item_option
    DROP FOREIGN KEY fk_option_history;

-- auction_history의 PK 변경 후 기존 id 제거
ALTER TABLE auction_history
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (auction_buy_id),
    DROP COLUMN id;

-- auction_item_option comment 변경
ALTER TABLE auction_item_option
    MODIFY auction_history_id varchar(255) NULL COMMENT 'auction_history 테이블의 외래 키 (auction_history.auction_buy_id)';

-- auction_item_option -> auction_history 제약 조건 추가
ALTER TABLE auction_item_option
    ADD CONSTRAINT fk_option_history
        FOREIGN KEY (auction_history_id) REFERENCES auction_history (auction_buy_id)
            ON DELETE CASCADE;