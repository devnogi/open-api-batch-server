CREATE TABLE batch_execution_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_type   VARCHAR(50)   NOT NULL COMMENT 'AUCTION_HISTORY_BATCH | ITEM_INFO_SYNC | METALWARE_ATTRIBUTE_SYNC',
    trigger_type TINYINT      NOT NULL COMMENT '0: 자동(Scheduler), 1: 수동(API 호출)',
    started_at   DATETIME(3)  NOT NULL,
    completed_at DATETIME(3)  NULL,
    is_success   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '0: 실패, 1: 성공',
    record_count INT          NOT NULL DEFAULT 0,
    message      VARCHAR(1000) NULL,
    INDEX idx_batch_type_started_at  (batch_type, started_at),
    INDEX idx_started_at  (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='배치 및 동기화 기능 실행 이력';
