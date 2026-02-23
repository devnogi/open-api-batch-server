-- V25: full_option_rate 컬럼 타입을 Hibernate 검증 스펙과 일치시키기
ALTER TABLE enchant_info
    MODIFY COLUMN full_option_rate INT NULL COMMENT '성공시 풀옵션 확률';
