ALTER TABLE auction_item_option
    CHANGE COLUMN item_option_id id VARCHAR(36) NOT NULL COMMENT 'ItemOption의 고유 식별자 (UUID)';