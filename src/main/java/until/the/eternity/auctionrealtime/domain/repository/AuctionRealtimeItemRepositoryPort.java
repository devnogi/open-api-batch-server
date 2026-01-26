package until.the.eternity.auctionrealtime.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.common.enums.ItemCategory;

/** AuctionRealtimeItem Repository Port (Hexagonal Architecture). */
public interface AuctionRealtimeItemRepositoryPort {

    /**
     * 해당 subcategory의 최신 date_auction_expire를 조회한다.
     *
     * @param category 아이템 카테고리
     * @return 최신 date_auction_expire (없으면 Optional.empty())
     */
    Optional<Instant> findLatestDateAuctionExpireBySubCategory(ItemCategory category);

    /**
     * 해당 subcategory & date_auction_expire에 해당하는 모든 레코드를 삭제한다.
     *
     * @param category 아이템 카테고리
     * @param dateAuctionExpire 만료 시각
     * @return 삭제된 레코드 수
     */
    int deleteBySubCategoryAndDateAuctionExpire(ItemCategory category, Instant dateAuctionExpire);

    /**
     * date_auction_expire가 현재 시각보다 이전인 모든 레코드를 삭제한다.
     *
     * @param now 현재 시각
     * @return 삭제된 레코드 수
     */
    int deleteExpiredItems(Instant now);

    /**
     * 엔티티들을 일괄 저장한다.
     *
     * @param entities 저장할 엔티티 리스트
     */
    void saveAll(List<AuctionRealtimeItem> entities);
}
