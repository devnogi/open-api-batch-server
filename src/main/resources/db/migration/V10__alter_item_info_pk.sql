ALTER TABLE item_info
DROP PRIMARY KEY,
ADD PRIMARY KEY (name, sub_category, top_category);