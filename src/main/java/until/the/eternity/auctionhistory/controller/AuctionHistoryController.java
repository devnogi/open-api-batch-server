package until.the.eternity.auctionhistory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.auctionhistory.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.auctionhistory.service.AuctionHistoryService;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

@RestController
@RequiredArgsConstructor
public class AuctionHistoryController {

    private final AuctionHistoryService auctionHistoryService;

    @GetMapping("/auction-history/search")
    public ResponseEntity<PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>>> search(
            @ModelAttribute PageRequestDto pageDto,
            @ModelAttribute @Valid AuctionHistorySearchRequest requestDto) {
        PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> result =
                auctionHistoryService.search(requestDto, pageDto);
        return ResponseEntity.ok(result);
    }
}
