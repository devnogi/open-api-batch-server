package until.the.eternity.auctionhistory.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionHistoryJpaRepository
        extends JpaRepository<AuctionHistory, Long>, JpaSpecificationExecutor<AuctionHistory> {

    List<AuctionHistory> findAllByAuctionBuyIdIn(List<String> auctionBuyIds);

    boolean existsByAuctionBuyIdIn(List<String> ids);

    @Query(
            """
           select a.auctionBuyId
             from AuctionHistory a
            where a.auctionBuyId in :ids
           """)
    List<String> findExistingIds(List<String> ids);

    @EntityGraph(attributePaths = "itemOptions")
    Optional<AuctionHistory> findWithItemOptionsById(Long id);
}
