package until.the.eternity.auction.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.auction.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auction.domain.model.AuctionHistory;

public interface AuctionHistoryRepositoryCustom {
    Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable);
}
