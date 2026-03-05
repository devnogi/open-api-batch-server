package until.the.eternity.batchlog.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.batchlog.application.service.BatchExecutionLogService;
import until.the.eternity.batchlog.domain.entity.BatchExecutionLog;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.batchlog.interfaces.rest.dto.response.BatchExecutionLogResponse;
import until.the.eternity.common.response.PageResponseDto;

@RestController
@RequestMapping("/api/batch-logs")
@RequiredArgsConstructor
@Tag(name = "Batch Execution Log", description = "배치 실행 로그 조회 API")
public class BatchExecutionLogController {

    private final BatchExecutionLogService batchExecutionLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "배치 종류별 실행 로그 페이지네이션 조회",
            description = "특정 배치 종류의 실행 로그를 최신순으로 페이지네이션하여 조회합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ResponseEntity<PageResponseDto<BatchExecutionLogResponse>> getBatchLogs(
            @RequestParam BatchType batchType,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(10) @Max(50) int size) {
        Page<BatchExecutionLog> result =
                batchExecutionLogService.findByBatchType(batchType, page, size);
        return ResponseEntity.ok(PageResponseDto.of(result.map(BatchExecutionLogResponse::from)));
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "배치 종류별 최신 실행 로그 조회",
            description =
                    "각 배치 종류(AUCTION_HISTORY_BATCH, ITEM_INFO_SYNC, METALWARE_ATTRIBUTE_SYNC)의 가장 최근 실행 로그를 하나씩 조회합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ResponseEntity<List<BatchExecutionLogResponse>> getLatestBatchLogs() {
        List<BatchExecutionLog> result = batchExecutionLogService.findLatestPerBatchType();
        return ResponseEntity.ok(result.stream().map(BatchExecutionLogResponse::from).toList());
    }
}
