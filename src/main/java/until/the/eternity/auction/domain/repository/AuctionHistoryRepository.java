package until.the.eternity.auction.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.auction.domain.model.AuctionHistory;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long> {
    List<AuctionHistory> findAllByAuctionBuyIdIn(List<String> auctionBuyIds);
}
