delete from item_option_value_info;
insert into item_option_value_info (option_type, option_sub_type, option_value, option_value2, option_desc)
select distinct option_type, option_sub_type, option_value, option_value2, option_desc from auction_item_option;