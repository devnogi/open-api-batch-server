-- 경매 검색 옵션 메타데이터 테이블 생성
CREATE TABLE `auction_search_option_metadata` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `search_option_name` VARCHAR(100) NOT NULL COMMENT '검색 옵션명 (한글)',
    `search_condition_json` JSON NOT NULL COMMENT '검색 조건 (파라미터명:타입)',
    `display_order` INT NOT NULL UNIQUE COMMENT '정렬 순서 (고유값)',
    `is_active` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (`id`),
    INDEX `idx_display_order` (`display_order`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='경매 검색 옵션 메타데이터';
