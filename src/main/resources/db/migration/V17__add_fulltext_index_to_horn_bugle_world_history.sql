-- FULLTEXT 인덱스를 위한 date_send 문자열 컬럼 추가 (Generated Column)
ALTER TABLE horn_bugle_world_history
    ADD COLUMN date_send_text VARCHAR(30) GENERATED ALWAYS AS (DATE_FORMAT(date_send, '%Y-%m-%d %H:%i:%s')) STORED;

-- character_name, message, server_name, date_send_text에 FULLTEXT 인덱스 추가
-- MySQL 8.0+ 에서 ngram parser를 사용하여 한글 검색 지원
ALTER TABLE horn_bugle_world_history
    ADD FULLTEXT INDEX ft_horn_bugle_search (character_name, message, server_name, date_send_text) WITH PARSER ngram;
