package until.the.eternity.auctionhistory.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;

/** 경매장 거래 내역 POJO Repository - Mock 또는 Stub 으로 대체해 단위 테스트 용이성 확보 */
public interface AuctionHistoryRepositoryPort {

    List<AuctionHistory> findAllByAuctionBuyIds(List<String> auctionBuyIds);

    Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable);

    Optional<AuctionHistory> findByIdWithOptions(Long id);

    boolean existsByAuctionBuyIds(List<String> ids);

    List<String> findExistingIds(List<String> ids);

    boolean existsByAuctionBuyIdIn(List<String> ids);

    Optional<AuctionHistory> findById(Long id);

    void saveAll(List<AuctionHistory> newEntities);
}
