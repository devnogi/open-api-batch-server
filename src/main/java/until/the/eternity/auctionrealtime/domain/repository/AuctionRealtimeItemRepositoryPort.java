package until.the.eternity.auctionrealtime.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** AuctionRealtimeItem Repository Port (Hexagonal Architecture). */
public interface AuctionRealtimeItemRepositoryPort {

    /**
     * 실시간 경매장 아이템을 검색한다.
     *
     * @param condition 검색 조건
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    Page<AuctionRealtimeItem> search(AuctionRealtimeSearchRequest condition, Pageable pageable);

    /**
     * ID로 실시간 경매장 아이템을 조회한다.
     *
     * @param id 아이템 ID
     * @return 아이템 (없으면 Optional.empty())
     */
    Optional<AuctionRealtimeItem> findById(Long id);

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
