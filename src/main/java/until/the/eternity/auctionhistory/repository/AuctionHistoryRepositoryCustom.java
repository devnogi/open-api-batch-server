package until.the.eternity.auctionhistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.auctionhistory.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;

public interface AuctionHistoryRepositoryCustom {
    Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable);
}
