package until.the.eternity.auction.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.auction.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.service.AuctionHistoryService;

@RestController
@RequiredArgsConstructor
public class AuctionHistoryController {

    private final AuctionHistoryService auctionHistoryService;

    @GetMapping("/auction-history/search")
    public ResponseEntity<Page<AuctionHistory>> search(
            AuctionHistorySearchRequest condition, Pageable pageable) {

        Page<AuctionHistory> auctionHistories = auctionHistoryService.search(condition, pageable);

        return ResponseEntity.ok(auctionHistories);
    }
}
