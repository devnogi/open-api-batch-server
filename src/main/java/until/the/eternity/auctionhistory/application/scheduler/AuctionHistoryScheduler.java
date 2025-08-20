package until.the.eternity.auctionhistory.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryService;
import until.the.eternity.auctionhistory.application.service.fetcher.AuctionHistoryFetcher;
import until.the.eternity.auctionhistory.application.service.persister.AuctionHistoryPersister;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryScheduler {

    private final AuctionHistoryService service;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "${openapi.auction-history.cron}", zone = "Asia/Seoul")
    public void fetchAndSaveAuctionHistoryAll() {
        List<AuctionHistory> newEntities = new ArrayList<>();
        for (ItemCategory category : ItemCategory.values()) {
            try {
                List<OpenApiAuctionHistoryResponse> fetchedDtos = fetcher.fetch(category);
                List<AuctionHistory> entities = persister.filterOutExisting(fetchedDtos, category);
                newEntities.addAll(entities);
            } catch (Exception e) {
                log.error(
                        ">[SCHEDULE] Error during processing category [{}]",
                        category.getSubCategory(),
                        e);
            }
            delayBetweenRequests();
        }
        service.saveAll(newEntities);
        log.info(
                ">[SCHEDULE] AuctionHistoryScheduler saved [{}] new auction history records complete",
                newEntities.size());
    }

    private void delayBetweenRequests() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn(">[SCHEDULE] Interrupted during delay between requests", e);
        }
    }
}
