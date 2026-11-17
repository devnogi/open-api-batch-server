-- Operational recovery may have already replaced the key on the empty weekly table.
SET @identity_key_ready := (
    SELECT COUNT(*) FROM (
        SELECT index_name FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'item_weekly_statistics'
          AND index_name = 'uk_item_weekly_identity_start' AND non_unique = 0
        GROUP BY index_name
        HAVING COUNT(*) = 4 AND COUNT(sub_part) = 0 AND GROUP_CONCAT(column_name ORDER BY seq_in_index) = 'item_name,item_top_category,item_sub_category,week_start_date'
    ) exact_index
);
SET @old_key_present := EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'item_weekly_statistics'
      AND index_name = 'uk_item_weekly_statistics_item_name_year_week'
);
SET @ddl := IF(@identity_key_ready = 1 AND NOT @old_key_present, 'SELECT 1',
    CONCAT('ALTER TABLE item_weekly_statistics ', CONCAT_WS(', ',
        IF(@old_key_present, 'DROP INDEX uk_item_weekly_statistics_item_name_year_week', NULL),
        IF(@identity_key_ready = 0, 'ADD UNIQUE KEY uk_item_weekly_identity_start (item_name, item_top_category, item_sub_category, week_start_date)', NULL))));
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
