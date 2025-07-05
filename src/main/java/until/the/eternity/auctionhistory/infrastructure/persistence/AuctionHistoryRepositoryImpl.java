package until.the.eternity.auctionhistory.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepository;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;

/** AuctionHistoryRepository Interface 구현체 */
@Repository
@RequiredArgsConstructor
public class AuctionHistoryRepositoryImpl implements AuctionHistoryRepository {

    private final AuctionHistoryJpaRepository jpaRepository;
    private final AuctionHistoryQueryDslRepository queryDslRepository;

    @Override
    public List<AuctionHistory> findAllByAuctionBuyIds(List<String> auctionBuyIds) {
        return jpaRepository.findAllByAuctionBuyIdIn(auctionBuyIds);
    }

    @Override
    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        return queryDslRepository.search(condition, pageable);
    }

    @Override
    public Optional<AuctionHistory> findByIdWithOptions(Long id) {
        return jpaRepository.findWithItemOptionsById(id);
    }

    @Override
    public boolean existsByAuctionBuyIds(List<String> ids) {
        return jpaRepository.existsByAuctionBuyIdIn(ids);
    }

    @Override
    public List<String> findExistingIds(List<String> ids) {
        return jpaRepository.findExistingIds(ids);
    }

    @Override
    public boolean existsByAuctionBuyIdIn(List<String> ids) {
        return jpaRepository.existsByAuctionBuyIdIn(ids);
    }

    @Override
    public Optional<AuctionHistory> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void saveAll(List<AuctionHistory> newEntities) {
        jpaRepository.saveAll(newEntities);
    }
}
