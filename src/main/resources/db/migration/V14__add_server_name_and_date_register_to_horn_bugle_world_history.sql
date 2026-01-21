-- horn_bugle_world_history 테이블에 server_name, date_register 컬럼 추가 및 인덱스 생성

-- 1. server_name 컬럼 추가 (서버 구분용)
ALTER TABLE horn_bugle_world_history
    ADD COLUMN server_name VARCHAR(20) NOT NULL DEFAULT '' COMMENT '서버 이름 (류트, 만돌린, 하프, 울프)';

-- 2. date_register 컬럼 추가 (수집 시각)
ALTER TABLE horn_bugle_world_history
    ADD COLUMN date_register DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '해당 뿔피리 내역을 수집한 시각';

-- 3. 복합 인덱스 생성 (server_name, date_send desc) - 서버별 최신 조회 최적화
CREATE INDEX idx_horn_bugle_server_date_send ON horn_bugle_world_history (server_name, date_send DESC);

-- 4. 단일 인덱스 생성 (date_send desc) - 전체 최신 조회 최적화
CREATE INDEX idx_horn_bugle_date_send ON horn_bugle_world_history (date_send DESC);
