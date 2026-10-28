-- V24: enchant_rank 컬럼 타입을 Hibernate 검증 스펙과 일치시키기
ALTER TABLE enchant_info
    MODIFY COLUMN enchant_rank VARCHAR(1) NOT NULL COMMENT '랭크';
