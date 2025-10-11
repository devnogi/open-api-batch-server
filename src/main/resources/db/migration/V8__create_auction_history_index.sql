create index idx_top_sub_item
    on auction_history (item_top_category, item_sub_category, item_name);