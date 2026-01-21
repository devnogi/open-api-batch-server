package until.the.eternity.auctionhistory.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** AuctionHistoryRepository Interface 구현체 */
@Repository
@RequiredArgsConstructor
public class AuctionHistoryRepositoryPortImpl implements AuctionHistoryRepositoryPort {

    private final AuctionHistoryJpaRepository jpaRepository;
    private final AuctionHistoryQueryDslRepository queryDslRepository;
    private final EntityManager em;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:500}")
    private int batchSize;

    @Override
    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        return queryDslRepository.search(condition, pageable);
    }

    @Override
    public Optional<AuctionHistory> findById(String id) {
        return jpaRepository.findById(id);
    }

    @Override
    @Transactional
    public void saveAll(List<AuctionHistory> newEntities) {
        if (newEntities.isEmpty()) {
            return;
        }

        for (int i = 0; i < newEntities.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, newEntities.size());
            List<AuctionHistory> subList = newEntities.subList(i, toIndex);
            jpaRepository.saveAll(subList);
            em.flush();
            em.clear();
        }
    }

    @Override
    public Optional<Instant> findLatestDateAuctionBuyBySubCategory(ItemCategory itemCategory) {
        return jpaRepository.findLatestDateAuctionBuyBySubCategory(
                itemCategory.getTopCategory(), itemCategory.getSubCategory());
    }

    @Override
    public List<Object[]> findDistinctItemInfo() {
        return jpaRepository.findDistinctItemInfo();
    }
}
