package until.the.eternity.batchlog.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.domain.enums.TriggerType;

import java.time.LocalDateTime;

@Schema(description = "배치 실행 로그 응답 DTO")
public record BatchExecutionLogResponse(
        @Schema(description = "로그 ID") Long id,
        @Schema(description = "배치 종류") BatchType batchType,
        @Schema(description = "실행 유형 (AUTO: 자동, MANUAL: 수동)") TriggerType triggerType,
        @Schema(description = "실행 시작 시각") LocalDateTime startedAt,
        @Schema(description = "실행 완료 시각 (null이면 미완료)") LocalDateTime completedAt,
        @Schema(description = "성공 여부") boolean isSuccess,
        @Schema(description = "처리 건수") int recordCount,
        @Schema(description = "결과 메시지") String message) {

    public static BatchExecutionLogResponse from(BatchExecutionLog entity) {
        return new BatchExecutionLogResponse(
                entity.getId(),
                entity.getBatchType(),
                entity.getTriggerType(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.isSuccess(),
                entity.getRecordCount(),
                entity.getMessage());
    }
}
