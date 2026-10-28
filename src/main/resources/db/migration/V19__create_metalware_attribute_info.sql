-- V19: 세공 능력치 정보 테이블 생성
CREATE TABLE metalware_attribute_info
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    metalware VARCHAR(50)  NOT NULL COMMENT '세공',
    level     TINYINT      NULL COMMENT '레벨',
    attribute VARCHAR(100) NULL COMMENT '능력치 효과',
    UNIQUE KEY uk_metalware_level (metalware, level)
) COMMENT '세공 능력치 정보 테이블';
