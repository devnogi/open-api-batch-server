package until.the.eternity.auctionrealtime.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
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
