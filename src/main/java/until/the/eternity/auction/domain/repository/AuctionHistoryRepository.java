package until.the.eternity.auction.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import until.the.eternity.auction.domain.model.AuctionHistory;

@Repository
public interface AuctionHistoryRepository
        extends JpaRepository<AuctionHistory, Long>,
                JpaSpecificationExecutor<AuctionHistory>,
                AuctionHistoryRepositoryCustom {
    List<AuctionHistory> findAllByAuctionBuyIdIn(List<String> auctionBuyIds);

    @Override
    Page<AuctionHistory> findAll(Specification<AuctionHistory> spec, Pageable pageable);

    @Query(
            "SELECT ah FROM AuctionHistory ah "
                    + "LEFT JOIN FETCH ah.itemOptions "
                    + "WHERE ah.id = :id")
    Optional<AuctionHistory> findById(@Param("id") Long id);

    boolean existsByAuctionBuyIdIn(List<String> ids);
}
