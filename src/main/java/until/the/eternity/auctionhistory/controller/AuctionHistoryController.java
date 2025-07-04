package until.the.eternity.auctionhistory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.auctionhistory.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.auctionhistory.service.AuctionHistoryScheduler;
import until.the.eternity.auctionhistory.service.AuctionHistoryService;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

@RequestMapping("/auction-history")
@RestController
@RequiredArgsConstructor
@Tag(name = "경매장 거래 내역 API", description = "경매장 거래 내역 API")
public class AuctionHistoryController {

    private final AuctionHistoryService auctionHistoryService;
    private final AuctionHistoryScheduler auctionHistoryScheduler;

    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>>> search(
            @ModelAttribute PageRequestDto pageDto,
            @ModelAttribute @Valid AuctionHistorySearchRequest requestDto) {
        PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> result =
                auctionHistoryService.search(requestDto, pageDto);
        return ResponseEntity.ok(result);
    }

    // TODO: 응답 형식도 몇건씩 저장됐는지 결과를 보내줘야하나 고민
    // TODO: 실행 시 OPEN API Key를 따로 받을지 고민
    @PostMapping("/batch")
    @Operation(
            summary = "경매장 거래 내역 배치 실행",
            description = "Nexon Open API 경매장 거래 내역 모든 카테고리 데이터 INSERT 배치 실행")
    public ResponseEntity<Void> triggerMinPriceBatch() {
        auctionHistoryScheduler.fetchAndSaveAuctionHistoryAll();
        return ResponseEntity.ok().build();
    }
}
