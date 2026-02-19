package until.the.eternity.batchlog.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
            summary = "배치 실행 로그 조회",
            description = "배치 실행 로그를 조건별로 페이지네이션하여 조회합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ResponseEntity<PageResponseDto<BatchExecutionLogResponse>> getBatchLogs(
            @RequestParam(required = false) BatchType batchType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate toDate,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(10) @Max(50) int size) {
        Page<BatchExecutionLog> result =
                batchExecutionLogService.search(batchType, fromDate, toDate, page, size);
        PageResponseDto<BatchExecutionLogResponse> response =
                PageResponseDto.of(result.map(BatchExecutionLogResponse::from));
        return ResponseEntity.ok(response);
    }
}
