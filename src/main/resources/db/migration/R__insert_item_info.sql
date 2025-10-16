delete from item_info;
INSERT INTO item_info (name, top_category, sub_category)
select distinct item_name, item_top_category, item_sub_category from auction_history;