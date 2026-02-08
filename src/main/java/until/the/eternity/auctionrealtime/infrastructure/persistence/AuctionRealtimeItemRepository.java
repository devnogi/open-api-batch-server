package until.the.eternity.auctionrealtime.infrastructure.persistence;

import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;

/** AuctionRealtimeItem JPA Repository. */
public interface AuctionRealtimeItemRepository extends JpaRepository<AuctionRealtimeItem, Long> {

    @Modifying
    @Query("DELETE FROM AuctionRealtimeItem a " + "WHERE a.itemSubCategory = :subCategory")
    int deleteBySubCategory(@Param("subCategory") String subCategory);

    @Modifying
    @Query("DELETE FROM AuctionRealtimeItem a WHERE a.dateAuctionExpire < :now")
    int deleteExpiredItems(@Param("now") Instant now);
}
