package until.the.eternity.auction.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auction.domain.component.AuctionHistoryFetcher;
import until.the.eternity.auction.domain.component.AuctionHistoryPersister;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auction.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepository repository;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "0 */10 * * * *")
    public void fetchAndSaveAuctionHistoryAll() {
        for (ItemCategory category : ItemCategory.values()) {
            try {
                fetchAndSaveAuctionHistory(category);
            } catch (Exception e) {
                log.error("Error during processing category [{}]", category.getSubCategory(), e);
            }
            delayBetweenRequests();
        }
    }

    private void fetchAndSaveAuctionHistory(ItemCategory category) {
        List<OpenApiAuctionHistoryResponse> dtoList = fetcher.fetch(category);

        if (dtoList == null || dtoList.isEmpty()) {
            log.info("[{}] No auction history data received", category.getSubCategory());
            return;
        }

        persister.saveIfNotExists(dtoList, category);
    }

    private void delayBetweenRequests() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during delay between requests", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        return repository.search(condition, pageable);
    }

    public AuctionHistory findByIdOrElseThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("에러 발생생"));
    }
}
