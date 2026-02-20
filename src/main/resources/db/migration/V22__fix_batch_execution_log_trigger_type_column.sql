ALTER TABLE batch_execution_log
    MODIFY COLUMN trigger_type TINYINT NOT NULL COMMENT '0: 자동(Scheduler), 1: 수동(API 호출)';
