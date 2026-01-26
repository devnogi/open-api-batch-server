package until.the.eternity.auctionrealtime.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.common.enums.ItemCategory;

/** AuctionRealtimeItemRepositoryPort 구현체. */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuctionRealtimeItemRepositoryPortImpl implements AuctionRealtimeItemRepositoryPort {

    private static final int BATCH_SIZE = 500;

    private final AuctionRealtimeItemRepository jpaRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<Instant> findLatestDateAuctionExpireBySubCategory(ItemCategory category) {
        return jpaRepository.findLatestDateAuctionExpireBySubCategory(category.getSubCategory());
    }

    @Override
    public int deleteBySubCategoryAndDateAuctionExpire(
            ItemCategory category, Instant dateAuctionExpire) {
        return jpaRepository.deleteBySubCategoryAndDateAuctionExpire(
                category.getSubCategory(), dateAuctionExpire);
    }

    @Override
    public int deleteExpiredItems(Instant now) {
        return jpaRepository.deleteExpiredItems(now);
    }

    @Override
    public void saveAll(List<AuctionRealtimeItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));

            if ((i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        // 마지막 배치 처리
        entityManager.flush();
        entityManager.clear();

        log.debug("[REALTIME] Saved {} auction realtime items", entities.size());
    }
}
