package until.the.eternity.auctionhistory.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Query(
            """
           select distinct a.itemName, a.itemTopCategory, a.itemSubCategory
             from AuctionHistory a
           """)
    List<Object[]> findDistinctItemInfo();

    @Query(
            """
           select a.auctionBuyId
             from AuctionHistory a
            where a.itemTopCategory = :topCategory
              and a.itemSubCategory = :subCategory
              and a.dateAuctionBuy = (
                  select max(a2.dateAuctionBuy)
                    from AuctionHistory a2
                   where a2.itemTopCategory = :topCategory
                     and a2.itemSubCategory = :subCategory
              )
           """)
    List<String> findAuctionBuyIdsByLatestDateAndSubCategory(
            String topCategory, String subCategory);
}
