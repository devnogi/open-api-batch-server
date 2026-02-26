package until.the.eternity.auctionrealtime.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.auctionrealtime.application.service.AuctionRealtimeService;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.RealtimePageRequestDto;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;
import until.the.eternity.common.response.PageResponseDto;

@RequestMapping("/auction-realtime")
@RestController
@RequiredArgsConstructor
@Tag(name = "실시간 경매장 API", description = "실시간 경매장 아이템 조회 API")
public class AuctionRealtimeController {

    private final AuctionRealtimeService service;

    @GetMapping("/search")
    @Operation(summary = "실시간 경매장 아이템 검색", description = "실시간 경매장에 등록된 아이템 검색")
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
    public ResponseEntity<
                    PageResponseDto<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>>>
            search(
                    @ParameterObject @ModelAttribute @Valid RealtimePageRequestDto pageDto,
                    @ParameterObject @ModelAttribute @Valid
                            AuctionRealtimeSearchRequest requestDto) {
        PageResponseDto<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> result =
                service.search(requestDto, pageDto.toPageable());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "실시간 경매장 아이템 단건 조회", description = "실시간 경매장 아이템 ID로 단건 조회")
    public ResponseEntity<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> findById(
            @PathVariable Long id) {
        AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> result =
                service.findByIdOrElseThrow(id);
        return ResponseEntity.ok(result);
    }
}
