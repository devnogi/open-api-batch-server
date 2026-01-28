package until.the.eternity.auctionrealtime.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;

import java.time.Instant;
import java.util.Optional;

/** AuctionRealtimeItem JPA Repository. */
public interface AuctionRealtimeItemRepository extends JpaRepository<AuctionRealtimeItem, Long> {

    @Query(
            value =
                    "SELECT date_auction_expire FROM auction_realtime_item "
                            + "WHERE item_sub_category = :subCategory "
                            + "ORDER BY date_auction_expire DESC LIMIT 1",
            nativeQuery = true)
    Optional<Instant> findLatestDateAuctionExpireBySubCategory(
            @Param("subCategory") String subCategory);

    @Modifying
    @Query(
            "DELETE FROM AuctionRealtimeItem a "
                    + "WHERE a.itemSubCategory = :subCategory AND a.dateAuctionExpire = :dateAuctionExpire")
    int deleteBySubCategoryAndDateAuctionExpire(
            @Param("subCategory") String subCategory,
            @Param("dateAuctionExpire") Instant dateAuctionExpire);

    @Modifying
    @Query("DELETE FROM AuctionRealtimeItem a WHERE a.dateAuctionExpire < :now")
    int deleteExpiredItems(@Param("now") Instant now);
}
