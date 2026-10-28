-- Create indexes for item_info table to improve query performance
-- These indexes support the ItemInfo search functionality with dynamic queries

-- Index for (top_category, sub_category, name)
CREATE INDEX idx_item_info_top_sub_name
    ON item_info (top_category, sub_category, name);

-- Index for (sub_category, name)
CREATE INDEX idx_item_info_sub_name
    ON item_info (sub_category, name);

-- Index for (name)
CREATE INDEX idx_item_info_name
    ON item_info (name);
