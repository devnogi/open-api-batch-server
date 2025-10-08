package until.the.eternity.auctionhistory.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;

@Repository
public interface AuctionHistoryJpaRepository
        extends JpaRepository<AuctionHistory, String>, JpaSpecificationExecutor<AuctionHistory> {

    List<AuctionHistory> findAllByAuctionBuyIdIn(List<String> auctionBuyIds);

    boolean existsByAuctionBuyIdIn(List<String> ids);

    @Query(
            """
           select a.auctionBuyId
             from AuctionHistory a
            where a.auctionBuyId in :ids
           """)
    List<String> findExistingIds(List<String> ids);

    @Query(
            """
           select MAX(a.dateAuctionBuy)
             from AuctionHistory a
            where a.itemTopCategory = :topCategory and a.itemSubCategory = :subCategory
           """)
    Optional<Instant> findLatestDateAuctionBuyBySubCategory(String topCategory, String subCategory);

    @EntityGraph(attributePaths = "itemOptions")
    Optional<AuctionHistory> findWithItemOptionsByAuctionBuyId(String id);
}
