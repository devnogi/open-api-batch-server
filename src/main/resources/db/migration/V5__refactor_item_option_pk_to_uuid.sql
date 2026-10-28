-- V5: Refactor auction_item_option primary key to UUID

-- 1. Drop foreign key constraints that reference this table's PK, if any. (none in this case)

-- 2. Modify the `id` column to remove the AUTO_INCREMENT property.
-- This is a separate step to avoid the error "Incorrect table definition".
ALTER TABLE auction_item_option MODIFY COLUMN id BIGINT NOT NULL;

-- 3. Drop the existing primary key constraint.
-- The `id` column still exists but loses its PK status.
ALTER TABLE auction_item_option DROP PRIMARY KEY;

-- 4. Drop the old `id` column.
ALTER TABLE auction_item_option DROP COLUMN id;

-- 5. Add the new UUID-based primary key column, naming it 'id' directly.
ALTER TABLE auction_item_option
    ADD COLUMN id VARCHAR(36) NOT NULL COMMENT 'ItemOption의 고유 식별자 (UUID)' FIRST,
    ADD PRIMARY KEY (id);
