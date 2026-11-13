-- Cover identity-only candidate windows in sitemap keyset order.
SET @index_exists := (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'item_info'
      AND index_name = 'idx_item_info_sitemap_identity'
);
SET @ddl := IF(
    @index_exists = 0,
    'CREATE INDEX idx_item_info_sitemap_identity ON item_info (name, top_category, sub_category)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
