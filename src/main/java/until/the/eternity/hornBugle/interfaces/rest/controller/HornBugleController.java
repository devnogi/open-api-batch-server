package until.the.eternity.hornBugle.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.hornBugle.application.scheduler.HornBugleScheduler;
import until.the.eternity.hornBugle.application.service.HornBugleService;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

@RequestMapping("/horn-bugle")
@RestController
@RequiredArgsConstructor
@Tag(name = "뿔피리 히스토리 API", description = "거대한 외침의 뿔피리 내역 API")
public class HornBugleController {

    private final HornBugleService service;
    private final HornBugleScheduler scheduler;

    @GetMapping
    @Operation(summary = "뿔피리 히스토리 조회", description = "거대한 외침의 뿔피리 내역을 조회합니다. 서버별 또는 전체 조회가 가능합니다.")
    public ResponseEntity<PageResponseDto<HornBugleHistoryResponse>> search(
            @Parameter(description = "서버 이름 (류트, 만돌린, 하프, 울프). 미입력시 전체 조회")
                    @RequestParam(required = false)
                    String serverName,
            @ParameterObject @ModelAttribute @Valid HornBuglePageRequestDto pageRequest) {
        PageResponseDto<HornBugleHistoryResponse> result = service.search(serverName, pageRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    @Operation(summary = "뿔피리 히스토리 배치 실행", description = "모든 서버의 거대한 외침의 뿔피리 내역을 수집하여 저장합니다.")
    public ResponseEntity<Void> triggerBatch() {
        scheduler.fetchAndSaveHornBugleHistoryAll();
        return ResponseEntity.ok().build();
    }
}
