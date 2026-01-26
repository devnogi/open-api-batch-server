package until.the.eternity.auctionrealtime.application.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.common.enums.ItemCategory;

/** 실시간 경매장 데이터 Service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRealtimeService {

    private final AuctionRealtimeItemRepositoryPort repository;

    /**
     * 해당 카테고리 & 동일 date_auction_expire 레코드 삭제 후 새 엔티티들을 저장한다.
     *
     * @param category 아이템 카테고리
     * @param dateAuctionExpire 삭제할 date_auction_expire
     * @param entities 저장할 엔티티 리스트
     */
    @Transactional
    public void deleteAndSave(
            ItemCategory category, Instant dateAuctionExpire, List<AuctionRealtimeItem> entities) {

        // 동일 date_auction_expire 레코드 삭제
        int deleted =
                repository.deleteBySubCategoryAndDateAuctionExpire(category, dateAuctionExpire);
        log.info(
                "[REALTIME] [{}] Deleted {} records with date_auction_expire={}",
                category.getSubCategory(),
                deleted,
                dateAuctionExpire);

        // 새 엔티티 저장
        repository.saveAll(entities);
        log.info(
                "[REALTIME] [{}] Saved {} new auction realtime items",
                category.getSubCategory(),
                entities.size());
    }

    /**
     * 엔티티들을 저장한다. (삭제 없이)
     *
     * @param entities 저장할 엔티티 리스트
     */
    @Transactional
    public void saveAll(List<AuctionRealtimeItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        repository.saveAll(entities);
        log.debug("[REALTIME] Saved {} auction realtime items", entities.size());
    }

    /**
     * 만료된 아이템을 삭제한다.
     *
     * @param now 현재 시각
     * @return 삭제된 레코드 수
     */
    @Transactional
    public int deleteExpiredItems(Instant now) {
        int deleted = repository.deleteExpiredItems(now);
        log.info("[REALTIME] Deleted {} expired auction realtime items", deleted);
        return deleted;
    }
}
