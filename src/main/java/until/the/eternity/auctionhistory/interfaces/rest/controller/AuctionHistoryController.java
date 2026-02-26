package until.the.eternity.auctionhistory.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.auctionhistory.application.scheduler.AuctionHistoryScheduler;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryService;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

@RequestMapping("/auction-history")
@RestController
@RequiredArgsConstructor
@Tag(name = "경매장 거래 내역 API", description = "경매장 거래 내역 API")
public class AuctionHistoryController {

    private final AuctionHistoryService service;
    private final AuctionHistoryScheduler scheduler;

    @GetMapping("/search")
    @Operation(summary = "경매장 거래 내역 검색", description = "Nexon Open API 경매장 거래 내역 검색")
    @Parameters({
        @Parameter(name = "metalwareSearchRequests[0].metalware", description = "첫 번째 세공 이름 (완전 일치)", in = ParameterIn.QUERY, schema = @Schema(type = "string", example = "불의 연성술")),
        @Parameter(name = "metalwareSearchRequests[0].levelFrom", description = "첫 번째 세공 레벨 시작값 (null이면 1로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", example = "1")),
        @Parameter(name = "metalwareSearchRequests[0].levelTo", description = "첫 번째 세공 레벨 종료값 (null이면 30으로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer", example = "30")),
        @Parameter(name = "metalwareSearchRequests[1].metalware", description = "두 번째 세공 이름 (완전 일치)", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "metalwareSearchRequests[1].levelFrom", description = "두 번째 세공 레벨 시작값 (null이면 1로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
        @Parameter(name = "metalwareSearchRequests[1].levelTo", description = "두 번째 세공 레벨 종료값 (null이면 30으로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
        @Parameter(name = "metalwareSearchRequests[2].metalware", description = "세 번째 세공 이름 (완전 일치)", in = ParameterIn.QUERY, schema = @Schema(type = "string")),
        @Parameter(name = "metalwareSearchRequests[2].levelFrom", description = "세 번째 세공 레벨 시작값 (null이면 1로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
        @Parameter(name = "metalwareSearchRequests[2].levelTo", description = "세 번째 세공 레벨 종료값 (null이면 30으로 처리)", in = ParameterIn.QUERY, schema = @Schema(type = "integer")),
    })
    public ResponseEntity<PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>>> search(
            @ParameterObject @ModelAttribute PageRequestDto pageDto,
            @ParameterObject @ModelAttribute @Valid AuctionHistorySearchRequest requestDto) {
        PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> result =
                service.search(requestDto, pageDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "경매장 거래 내역 단건 조회", description = "Nexon Open API 경매장 거래 내역 조회")
    public ResponseEntity<AuctionHistoryDetailResponse<ItemOptionResponse>> findById(
            @PathVariable String id) {
        AuctionHistoryDetailResponse<ItemOptionResponse> result = service.findByIdOrElseThrow(id);
        return ResponseEntity.ok(result);
    }

    // TODO: 응답 형식도 몇건씩 저장됐는지 결과를 보내줘야하나 고민
    // TODO: 실행 시 OPEN API Key를 따로 받을지 고민
    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "경매장 거래 내역 배치 실행",
            description =
                    "Nexon Open API 경매장 거래 내역 모든 카테고리 데이터 INSERT 배치 실행. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ResponseEntity<Void> triggerMinPriceBatch() {
        scheduler.fetchAndSaveAuctionHistoryAll();
        return ResponseEntity.ok().build();
    }
}
