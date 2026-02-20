-- V21: 인챈트 정보 테이블 생성
CREATE TABLE enchant_info
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    fullname         VARCHAR(100) NOT NULL COMMENT '인챈트 이름 및 랭크',
    name             VARCHAR(50)  NOT NULL COMMENT '인챈트 이름',
    enchant_rank     CHAR(1)      NOT NULL COMMENT '랭크',
    affix_position   VARCHAR(10)  NOT NULL COMMENT '접두 접미 구분',
    effect           VARCHAR(100) NULL     COMMENT '효과',
    acquired_info    VARCHAR(200) NULL     COMMENT '입수 정보',
    full_option_rate TINYINT      NULL     COMMENT '성공시 풀옵션 확률',
    is_exclusive     BOOLEAN      NULL     COMMENT '전용 인챈트 여부',
    is_common_part   BOOLEAN      NULL     COMMENT '전 부위 공용 인챈트 여부',
    UNIQUE KEY uk_enchant_info (name, enchant_rank, affix_position)
) COMMENT '인챈트 정보 테이블';
