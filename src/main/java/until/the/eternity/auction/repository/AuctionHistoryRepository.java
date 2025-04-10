package until.the.eternity.auction.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import until.the.eternity.auction.domain.model.AuctionHistory;

@Repository
public interface AuctionHistoryRepository extends JpaRepository<AuctionHistory, Long>, JpaSpecificationExecutor<AuctionHistory> {
    @Override
    Page<AuctionHistory> findAll(Specification<AuctionHistory> spec, Pageable pageable);
}
