-- 기존 FK, PK 제약 조건 삭제
ALTER TABLE auction_item_option
    DROP FOREIGN KEY fk_option_history,
    DROP PRIMARY KEY;

-- 기존 id 컬럼 삭제
ALTER TABLE auction_item_option
    DROP COLUMN id;

-- 새로운 PK 컬럼 추가
ALTER TABLE auction_item_option
    ADD COLUMN item_option_id VARCHAR(36) NOT NULL COMMENT 'ItemOption의 고유 식별자 (UUID)' FIRST,
    ADD PRIMARY KEY (item_option_id);

-- FK 제약 조건 다시 추가
ALTER TABLE auction_item_option
    ADD CONSTRAINT fk_option_history
        FOREIGN KEY (auction_history_id) REFERENCES auction_history (auction_buy_id)
            ON DELETE CASCADE;
