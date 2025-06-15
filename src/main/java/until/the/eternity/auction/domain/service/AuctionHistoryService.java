package until.the.eternity.auction.domain.service;

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
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.dto.AuctionHistorySearchCondition;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.common.exception.enums.ErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepository repository;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "0 0 */2 * * *")
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
        List<AuctionHistoryDto> dtoList = fetcher.fetch(category);

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
        }
    }

    @Transactional(readOnly = true)
    public Page<AuctionHistory> search(AuctionHistorySearchCondition condition, Pageable pageable) {
        return repository.search(condition, pageable);
    }

    @Transactional(readOnly = true)
    public AuctionHistory findByIdOrElseThrow(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ENTITY_NOT_FOUND));
    }
}
