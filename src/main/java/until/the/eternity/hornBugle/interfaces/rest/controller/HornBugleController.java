package until.the.eternity.hornBugle.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.hornBugle.application.scheduler.HornBugleScheduler;
import until.the.eternity.hornBugle.application.service.HornBugleService;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

@Slf4j
@RequestMapping("/horn-bugle")
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "뿔피리 히스토리 API", description = "거대한 외침의 뿔피리 내역 API")
public class HornBugleController {

    private final HornBugleService service;
    private final HornBugleScheduler scheduler;

    @GetMapping
    @Operation(
            summary = "뿔피리 히스토리 조회",
            description =
                    "거대한 외침의 뿔피리 내역을 조회합니다. 서버별 또는 전체 조회가 가능합니다. "
                            + "keyword 입력 시 Elasticsearch 기반 전문 검색을 수행합니다.")
    public ResponseEntity<PageResponseDto<HornBugleHistoryResponse>> search(
            @Parameter(description = "서버 이름 (류트, 만돌린, 하프, 울프). 미입력시 전체 조회")
                    @RequestParam(required = false)
                    String serverName,
            @Parameter(description = "검색 키워드 (캐릭터명, 메시지, 서버명, 발화시각 검색). 최대 50자")
                    @RequestParam(required = false)
                    @Size(max = 50, message = "검색 키워드는 최대 50자까지 입력 가능합니다.")
                    String keyword,
            @ParameterObject @ModelAttribute @Valid HornBuglePageRequestDto pageRequest) {
        PageResponseDto<HornBugleHistoryResponse> result =
                service.search(serverName, keyword, pageRequest);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/batch")
    @Operation(summary = "뿔피리 히스토리 배치 실행", description = "모든 서버의 거대한 외침의 뿔피리 내역을 수집하여 저장합니다.")
    public ResponseEntity<Void> triggerBatch() {
        log.info("[HornBugle] Batch API triggered");
        try {
            scheduler.fetchAndSaveHornBugleHistoryAll();
            log.info("[HornBugle] Batch API completed successfully");
        } catch (Exception e) {
            log.error("[HornBugle] Batch API failed: {}", e.getMessage(), e);
            throw e;
        }
        return ResponseEntity.ok().build();
    }
}
